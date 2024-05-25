package org.liuxp.minioplus.core.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.config.MinioPlusProperties;
import org.liuxp.minioplus.model.bo.CreateUploadUrlReqBO;
import org.liuxp.minioplus.model.bo.CreateUploadUrlRespBO;
import org.liuxp.minioplus.model.copy.ListPartsResultCopy;
import org.liuxp.minioplus.model.dto.FileCheckDTO;
import org.liuxp.minioplus.model.dto.FileMetadataInfoDTO;
import org.liuxp.minioplus.model.dto.FileMetadataInfoSaveDTO;
import org.liuxp.minioplus.model.dto.FileMetadataInfoUpdateDTO;
import org.liuxp.minioplus.model.dto.minio.MultipartUploadCreateDTO;
import org.liuxp.minioplus.model.enums.ResponseCodeEnum;
import org.liuxp.minioplus.model.enums.StorageBucketEnums;
import org.liuxp.minioplus.core.common.exception.MinioPlusBusinessException;
import org.liuxp.minioplus.core.common.utils.MinioPlusCommonUtil;
import org.liuxp.minioplus.model.vo.CompleteResultVo;
import org.liuxp.minioplus.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.model.vo.FileMetadataInfoVo;
import org.liuxp.minioplus.core.engine.StorageEngineService;
import org.liuxp.minioplus.core.repository.MetadataRepository;
import org.liuxp.minioplus.core.repository.MinioRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * 存储引擎Service接口实现类
 *
 * @author contact@liuxp.me
 * @since 2023/06/26
 */
@Service
@Slf4j
public class StorageEngineServiceImpl implements StorageEngineService {

    @Resource
    MetadataRepository metadataRepository;

    @Resource
    MinioPlusProperties properties;

    @Resource
    MinioRepository minioRepository;

    private static final String FAILURE_URL = "/storage/failure";

    /**
     * MinIO中上传编号名称
     */
    private static final String UPLOAD_ID = "uploadId";

    /**
     * 文件上传预检查
     *
     * 1.当前用户上传过，已完成，秒传
     * 2.当前用户上传过，未完成，断点续传
     * 3.其他用户上传过，未完成，断点续传，存储当前用户的记录
     * 4.其他用户上传过，已完成，秒传，存储当前用户的记录
     *
     * @param dto 文件预检查入参DTO
     * @return {@link FileCheckResultVo}
     */
    @Override
    public FileCheckResultVo check(FileCheckDTO dto,String userId) {

        // 根据MD5查询文件是否已上传过
        FileMetadataInfoDTO searchDTO = new FileMetadataInfoDTO();
        searchDTO.setFileMd5(dto.getFileMd5());
        List<FileMetadataInfoVo> list = metadataRepository.list(searchDTO);

        FileMetadataInfoSaveDTO saveDTO = new FileMetadataInfoSaveDTO();
        CreateUploadUrlReqBO bo = new CreateUploadUrlReqBO();

        // 没有任何用户上传过该文件，直接创建新的元数据与链接
        if(CollUtil.isEmpty(list)){
            // 获取上传链接
            BeanUtils.copyProperties(dto, bo);
            CreateUploadUrlRespBO createUploadUrlRespBO = this.createUploadUrl(bo);

            FileMetadataInfoVo metadataInfo = saveMetadataInfo(saveDTO, createUploadUrlRespBO, dto, userId);

            return this.buildResult(metadataInfo, createUploadUrlRespBO.getParts(), createUploadUrlRespBO.getPartCount(), Boolean.FALSE);
        }


        Optional<FileMetadataInfoVo> userUploaded = list.stream().filter(item -> userId.equals(item.getCreateUser())).findFirst();

        // 当前用户没有上传过
        if (!userUploaded.isPresent()) {
            // 优先寻找其他用户未上传完成的任务
            FileMetadataInfoVo otherUserUploadTask = list.stream()
                    .filter(FileMetadataInfoVo::getIsFinished)
                    .findAny()
                    .orElseGet(() -> list.stream()
                            .filter(item -> !item.getIsFinished()).findFirst().get());

            if (Boolean.FALSE.equals(otherUserUploadTask.getIsFinished())) {
                // 上传过未完成-断点续传
                bo.setIsSequel(Boolean.TRUE);
                CreateUploadUrlRespBO respBO = this.breakResume(otherUserUploadTask);

                // 插入自己的元数据
                BeanUtils.copyProperties(otherUserUploadTask, saveDTO);
                saveDTO.setFileName(dto.getFullFileName());
                saveDTO.setCreateUser(userId);
                saveDTO.setIsPrivate(dto.getIsPrivate());
                saveDTO.setUploadTaskId(respBO.getUploadTaskId());
                FileMetadataInfoVo metadataInfoVo = metadataRepository.save(saveDTO);

                return this.buildResult(metadataInfoVo, respBO.getParts(), respBO.getPartCount(), Boolean.FALSE);
            } else {
                // 秒传
                BeanUtils.copyProperties(otherUserUploadTask, saveDTO);
                saveDTO.setFileName(dto.getFullFileName());
                saveDTO.setCreateUser(userId);
                saveDTO.setIsPrivate(dto.getIsPrivate());
                FileMetadataInfoVo metadataInfoVo = metadataRepository.save(saveDTO);
                return this.buildResult(metadataInfoVo, new ArrayList<>(1), 0, Boolean.TRUE);
            }
        } else if (Boolean.TRUE.equals(userUploaded.get().getIsFinished())) {
            // 上传过并且已经完成- 秒传
            return this.buildResult(userUploaded.get(), new ArrayList<>(1), 0, Boolean.TRUE);
        } else {
            // 上传过未完成-断点续传
            String suffix = FileUtil.getSuffix(dto.getFullFileName());
            String bucketName = StorageBucketEnums.getBucketByFileSuffix(suffix);
            // 是图片类型
            if (bucketName.equals(StorageBucketEnums.IMAGE.getCode())) {
                // 分块信息集合
                List<FileCheckResultVo.Part> partList = new ArrayList<>();

                FileCheckResultVo.Part part = new FileCheckResultVo.Part();
                // 图片上传时，直接使用fileKey作为uploadId
                part.setUploadId(userUploaded.get().getFileKey());
                part.setUrl("/storage/upload/image/"+userUploaded.get().getFileKey());
                part.setStartPosition(0L);
                part.setEndPosition(dto.getFileSize());
                partList.add(part);

                return this.buildResult(userUploaded.get(), partList, 0, Boolean.FALSE);
            }

            bo.setIsSequel(Boolean.TRUE);
            CreateUploadUrlRespBO respBO = this.breakResume(userUploaded.get());
            if(CollUtil.isNotEmpty(respBO.getParts()) && !respBO.getUploadTaskId().equals(userUploaded.get().getUploadTaskId())){
                userUploaded.get().setUploadTaskId(respBO.getUploadTaskId());
                FileMetadataInfoUpdateDTO updateDTO = new FileMetadataInfoUpdateDTO();
                updateDTO.setId(userUploaded.get().getId());
                updateDTO.setUploadTaskId(userUploaded.get().getUploadTaskId());
                updateDTO.setUpdateUser(userId);
                metadataRepository.update(updateDTO);
            }

            return this.buildResult(userUploaded.get(), respBO.getParts(), respBO.getPartCount(), Boolean.FALSE);
        }
    }

    /**
     * 构建结果
     * 构建文件预检结果
     *
     * @param metadataInfo 元数据信息
     * @param partList     块信息
     * @param partCount    块数量
     * @param isDone       是否秒传
     * @return {@link FileCheckResultVo}
     */
    private FileCheckResultVo buildResult(FileMetadataInfoVo metadataInfo, List<FileCheckResultVo.Part> partList, Integer partCount, Boolean isDone) {
        FileCheckResultVo fileCheckResultVo = new FileCheckResultVo();
        // 主键
        fileCheckResultVo.setId(metadataInfo.getId());
        // 文件KEY
        fileCheckResultVo.setFileKey(metadataInfo.getFileKey());
        // 文件md5
        fileCheckResultVo.setFileMd5(metadataInfo.getFileMd5());
        // 文件名
        fileCheckResultVo.setFileName(metadataInfo.getFileName());
        // MIME类型
        fileCheckResultVo.setFileMimeType(metadataInfo.getFileMimeType());
        // 文件后缀
        fileCheckResultVo.setFileSuffix(metadataInfo.getFileSuffix());
        // 文件长度
        fileCheckResultVo.setFileSize(metadataInfo.getFileSize());
        // 是否秒传
        fileCheckResultVo.setIsDone(isDone);
        // 分块数量
        fileCheckResultVo.setPartCount(partCount);
        // 分块大小
        fileCheckResultVo.setPartSize(properties.getPart().getSize());
        // 分块信息
        fileCheckResultVo.setPartList(partList);
        return fileCheckResultVo;
    }

    /**
     * 保存文件源信息
     *
     * @param saveDTO  元数据保存实体类
     * @param createUploadUrlRespBO   上传链接参数
     * @param dto    文件检测参数
     * @param userId       用户
     * @return {@link FileMetadataInfoVo}
     */
    private FileMetadataInfoVo saveMetadataInfo(FileMetadataInfoSaveDTO saveDTO, CreateUploadUrlRespBO createUploadUrlRespBO,
                                                FileCheckDTO dto, String userId) {
        // 保存文件元数据
        String suffix = FileUtil.getSuffix(dto.getFullFileName());
        // 文件KEY
        saveDTO.setFileKey(createUploadUrlRespBO.getFileKey());
        // 文件md5
        saveDTO.setFileMd5(dto.getFileMd5());
        // 文件名
        saveDTO.setFileName(dto.getFullFileName());
        // MIME类型
        saveDTO.setFileMimeType(FileUtil.getMimeType(dto.getFullFileName()));
        // 文件后缀
        saveDTO.setFileSuffix(suffix);
        // 文件长度
        saveDTO.setFileSize(dto.getFileSize());

        // minio引擎特殊存储
        if (createUploadUrlRespBO.getMinioStorage() != null) {
            // 存储桶
            saveDTO.setStorageBucket(createUploadUrlRespBO.getMinioStorage().getBucketName());
            // 存储路径
            saveDTO.setStoragePath(createUploadUrlRespBO.getMinioStorage().getStoragePath());
        }
        // 上传任务id
        saveDTO.setUploadTaskId(createUploadUrlRespBO.getUploadTaskId());
        // 状态 0:未完成 1:已完成
        saveDTO.setIsFinished(Boolean.FALSE);
        // 是否分片
        saveDTO.setIsPart(createUploadUrlRespBO.getPartCount() > 1);
        // 分片数量
        saveDTO.setPartNumber(createUploadUrlRespBO.getPartCount());
        // 预览图 0:无 1:有
        saveDTO.setIsPreview(saveDTO.getStorageBucket().equals(StorageBucketEnums.IMAGE.getCode()) && properties.getThumbnail().isEnable());
        // 是否私有 0:否 1:是
        saveDTO.setIsPrivate(dto.getIsPrivate());
        // 创建人
        saveDTO.setCreateUser(userId);
        // 修改人
        saveDTO.setUpdateUser(userId);
        return metadataRepository.save(saveDTO);
    }

    /**
     * 合并已分块的文件
     *
     * @param fileKey 文件关键
     * @return {@link Boolean}
     */
    @Override
    public CompleteResultVo complete(String fileKey, List<String> partMd5List,String userId) {

        CompleteResultVo completeResultVo = new CompleteResultVo();

        FileMetadataInfoDTO searchDto = new FileMetadataInfoDTO();
        // 用户id
        searchDto.setCreateUser(userId);
        // 文件key
        searchDto.setFileKey(fileKey);

        FileMetadataInfoVo metadata = metadataRepository.one(searchDto);

        if(metadata == null){
            log.error(fileKey+"不存在");
            throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(),fileKey+"不存在");
        }

        if(Boolean.TRUE.equals(metadata.getIsFinished())){
            // 如果文件已上传完成，直接返回true，不进行合并
            completeResultVo.setIsComplete(true);
            return completeResultVo;
        }

        if(metadata.getStorageBucket().equals(StorageBucketEnums.IMAGE.getCode())){
            // 图片时，生成图片的上传链接
            List<FileCheckResultVo.Part> partList = new ArrayList<>();

            FileCheckResultVo.Part part = new FileCheckResultVo.Part();
            part.setUploadId(metadata.getFileKey());
            part.setUrl("/storage/upload/image/"+metadata.getFileKey());
            part.setStartPosition(0L);
            part.setEndPosition(metadata.getFileSize());
            partList.add(part);

            completeResultVo.setIsComplete(false);
            completeResultVo.setUploadTaskId(metadata.getUploadTaskId());
            completeResultVo.setPartList(partList);

            return completeResultVo;
        }

        completeResultVo = this.completeMultipartUpload(metadata, partMd5List);

        if (Boolean.TRUE.equals(completeResultVo.getIsComplete())) {

            // 更新自己上传的文件元数据状态
            FileMetadataInfoUpdateDTO updateDTO = new FileMetadataInfoUpdateDTO();
            updateDTO.setId(metadata.getId());
            updateDTO.setIsFinished(Boolean.TRUE);
            updateDTO.setUpdateUser(metadata.getUpdateUser());
            metadataRepository.update(updateDTO);

            // 搜索数据库中所有未完成的相同MD5元数据，更新为完成状态
            searchDto = new FileMetadataInfoDTO();
            searchDto.setFileMd5(metadata.getFileMd5());
            searchDto.setIsFinished(false);
            List<FileMetadataInfoVo> others = metadataRepository.list(searchDto);
            if(CollUtil.isNotEmpty(others)){
                for (FileMetadataInfoVo other : others) {
                    updateDTO = new FileMetadataInfoUpdateDTO();
                    updateDTO.setId(other.getId());
                    updateDTO.setIsFinished(Boolean.TRUE);
                    updateDTO.setUpdateUser(metadata.getUpdateUser());
                    metadataRepository.update(updateDTO);
                }
            }
        }else{
            if(!metadata.getUploadTaskId().equals(completeResultVo.getUploadTaskId())){
                FileMetadataInfoUpdateDTO updateDTO = new FileMetadataInfoUpdateDTO();
                updateDTO.setId(metadata.getId());
                updateDTO.setUploadTaskId(completeResultVo.getUploadTaskId());
                updateDTO.setUpdateUser(metadata.getUpdateUser());
                metadataRepository.update(updateDTO);
            }
        }
        return completeResultVo;
    }

    @Override
    public Boolean uploadImage(String fileKey, byte[] file) {

        FileMetadataInfoDTO searchDto = new FileMetadataInfoDTO();
        searchDto.setFileKey(fileKey);
        FileMetadataInfoVo metadata = metadataRepository.one(searchDto);

        try {

            FileMetadataInfoSaveDTO saveDto = new FileMetadataInfoSaveDTO();
            saveDto.setStorageBucket(metadata.getStorageBucket());
            saveDto.setStoragePath(metadata.getStoragePath());
            saveDto.setFileMd5(metadata.getFileMd5());
            saveDto.setFileSize(metadata.getFileSize());
            saveDto.setFileMimeType(metadata.getFileMimeType());
            saveDto.setIsPreview(metadata.getIsPreview());

            Boolean isCreateFile = createFile(saveDto,file);

            FileMetadataInfoUpdateDTO updateDTO = new FileMetadataInfoUpdateDTO();
            updateDTO.setId(metadata.getId());
            updateDTO.setIsFinished(Boolean.TRUE);
            updateDTO.setUpdateUser(metadata.getUpdateUser());
            metadataRepository.update(updateDTO);

            return isCreateFile;

        }catch(Exception e){
            log.error("文件上传失败",e);
            throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(),"文件上传失败");
        }

    }

    @Override
    public String download(String fileKey, String userId) {

        FileMetadataInfoVo metadata = getFileMetadataInfo(fileKey, userId);

        try{

            // 文件权限校验，元数据为空或者当前登录用户不是文件所有者时抛出异常
            this.authentication(metadata, fileKey, userId);

            return minioRepository.getDownloadUrl(metadata.getFileName(),metadata.getFileMimeType(),metadata.getStorageBucket(),metadata.getStoragePath() + "/"+ metadata.getFileMd5());
        }catch(Exception e){
            // 打印日志
            log.error(e.getMessage());
            // 返回失效地址，任何异常，统一返回给前端图片已失效
            return FAILURE_URL;
        }
    }

    @Override
    public String image(String fileKey, String userId) {

        FileMetadataInfoVo metadata = getFileMetadataInfo(fileKey, userId);

        try{

            // 文件权限校验，元数据为空或者当前登录用户不是文件所有者时抛出异常
            this.authentication(metadata, fileKey, userId);

            return minioRepository.getPreviewUrl(metadata.getFileMimeType(),metadata.getStorageBucket(),metadata.getStoragePath() + "/"+ metadata.getFileMd5());

        }catch(Exception e){
            // 打印日志
            log.error(e.getMessage());
            // 返回失效地址，任何异常，统一返回给前端图片已失效
            return FAILURE_URL;
        }
    }

    @Override
    public String preview(String fileKey, String userId) {

        FileMetadataInfoVo metadata = getFileMetadataInfo(fileKey, userId);

        try{

            // 文件权限校验，元数据为空或者当前登录用户不是文件所有者时抛出异常
            this.authentication(metadata, fileKey, userId);
            // 判断是否存在缩略图，设置桶名称
            String bucketName = Boolean.TRUE.equals(metadata.getIsPreview()) ? StorageBucketEnums.IMAGE_PREVIEW.getCode() : metadata.getStorageBucket();
            // 创建图片预览地址
            return minioRepository.getPreviewUrl(metadata.getFileMimeType(),bucketName,metadata.getStoragePath() + "/"+ metadata.getFileMd5());

        }catch(Exception e){
            // 打印日志
            log.error(e.getMessage());
            // 返回失效地址，任何异常，统一返回给前端图片已失效
            return FAILURE_URL;
        }
    }

    @Override
    public Boolean createFile(FileMetadataInfoSaveDTO saveDTO, byte[] fileBytes) {

        // 写入文件
        minioRepository.write(saveDTO.getStorageBucket(), MinioPlusCommonUtil.getObjectName(saveDTO.getFileMd5()), new ByteArrayInputStream(fileBytes), saveDTO.getFileSize(), saveDTO.getFileMimeType());

        // 判断是否生成缩略图
        if(Boolean.TRUE.equals(saveDTO.getIsPreview())){

            try{
                ByteArrayOutputStream largeImage = MinioPlusCommonUtil.resizeImage(new ByteArrayInputStream(fileBytes), properties.getThumbnail().getSize());
                byte[] largeImageBytes = largeImage.toByteArray();
                minioRepository.write(StorageBucketEnums.IMAGE_PREVIEW.getCode(), MinioPlusCommonUtil.getObjectName(saveDTO.getFileMd5()), new ByteArrayInputStream(largeImageBytes), largeImageBytes.length, saveDTO.getFileMimeType());
            }catch(Exception e){
                log.error("缩略图写入失败",e);
                throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(),"缩略图写入失败");
            }
        }

        return true;


    }

    @Override
    public Pair<FileMetadataInfoVo,byte[]> read(String fileKey) {

        // 查询文件元数据
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileKey(fileKey);
        FileMetadataInfoVo fileMetadataInfoVo = metadataRepository.one(fileMetadataInfo);

        if (null == fileMetadataInfoVo) {
            return null;
        }

        // 读取流
        byte[] fileBytes = minioRepository.read(fileMetadataInfoVo.getStorageBucket(), fileMetadataInfoVo.getStoragePath() + "/" +  fileMetadataInfoVo.getFileMd5());

        return Pair.of(fileMetadataInfoVo,fileBytes);
    }

    @Override
    public Boolean remove(String fileKey) {

        // 查询元数据信息
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileKey(fileKey);
        FileMetadataInfoVo metadata = metadataRepository.one(fileMetadataInfo);

        if (null != metadata) {
            remove(metadata);
        }

        return true;
    }

    @Override
    public Boolean remove(String fileKey, String userId) {

        // 查询元数据信息
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileKey(fileKey);
        FileMetadataInfoVo metadata = metadataRepository.one(fileMetadataInfo);

        if (null != metadata && userId.equals(metadata.getCreateUser())) {
            remove(metadata);
        }

        return true;
    }

    private void remove(FileMetadataInfoVo metadata){
        // 删除元数据信息
        metadataRepository.remove(metadata.getId());

        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileMd5(metadata.getFileMd5());
        List<FileMetadataInfoVo> metadataList = metadataRepository.list(fileMetadataInfo);

        if(CollUtil.isEmpty(metadataList)){
            // 当不存在任何该MD5值的文件元数据时，删除物理文件
            minioRepository.remove(metadata.getStorageBucket(), metadata.getStoragePath() + "/" +  metadata.getFileMd5());
            if(Boolean.TRUE.equals(metadata.getIsPreview())){
                // 当存在缩略图时，同步删除缩略图
                minioRepository.remove(StorageBucketEnums.IMAGE_PREVIEW.getCode(), metadata.getStoragePath() + "/" +  metadata.getFileMd5());
            }
        }
    }

    /**
     * 根据用户取得文件元数据信息
     * 当userId匹配时直接返回，不匹配时检查是否存在公有元数据
     * @param fileKey 文件KEY
     * @param userId  用户主键
     * @return 文件元数据信息
     */
    private FileMetadataInfoVo getFileMetadataInfo(String fileKey, String userId) {
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileKey(fileKey);
        // 取得md5元数据
        List<FileMetadataInfoVo> fileMetadataInfoVoList = metadataRepository.list(fileMetadataInfo);

        for (FileMetadataInfoVo fileMetadataInfoVo : fileMetadataInfoVoList) {
            if (Boolean.FALSE.equals(fileMetadataInfoVo.getIsPrivate()) || fileMetadataInfoVo.getCreateUser().equals(userId)) {
                return fileMetadataInfoVo;
            }
        }

        return null;
    }

    /**
     * 文件权限校验
     * 元数据为空时抛出异常
     * 文件为私有文件且当前登录用户不是文件所有者时抛出异常
     *
     * @param metadata 文件元数据
     * @param fileKey 文件key
     * @param userId 用户主键
     */
    private void authentication(FileMetadataInfoVo metadata, String fileKey, String userId){
        if (null == metadata) {
            throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(), fileKey + "不存在!");
        }

        // 元数据信息存在，判断权限
        if(Boolean.TRUE.equals(metadata.getIsPrivate()) && !userId.equals(metadata.getCreateUser())){
            throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(), fileKey + "用户userId="+userId+"没有访问权限!");
        }
    }

    /**
     * 计算分块的数量
     *
     * @param fileSize 文件大小
     * @return {@link Integer}
     */
    private Integer computeChunkNum(Long fileSize) {
        // 计算分块数量
        double tempNum = (double) fileSize / properties.getPart().getSize();
        // 向上取整
        return ((Double) Math.ceil(tempNum)).intValue();
    }


    /**
     * 构建响应给前端的分片信息
     *
     * @param uploadCreateDTO 分片dto
     * @param queryParams     查询参数
     * @param fileSize        文件大小
     * @param start           开始位置
     * @param partNumber      块号
     * @return {@link FileCheckResultVo.Part}
     */
    private FileCheckResultVo.Part buildResultPart(MultipartUploadCreateDTO uploadCreateDTO, Map<String, String> queryParams, Long fileSize, long start, Integer partNumber) {
        // 计算起始位置
        long end = Math.min(start + properties.getPart().getSize(), fileSize);
        queryParams.put("partNumber", String.valueOf(partNumber));
        String uploadUrl = minioRepository.getPresignedObjectUrl(uploadCreateDTO.getBucketName(), uploadCreateDTO.getObjectName(), queryParams);
        FileCheckResultVo.Part part = new FileCheckResultVo.Part();
        part.setUploadId(uploadCreateDTO.getUploadId());
        // 上传地址
        part.setUrl(uploadUrl);
        // 开始位置
        part.setStartPosition(start);
        // 结束位置
        part.setEndPosition(end);
        return part;
    }

    /**
     * 断点续传-创建断点的URL
     *
     * @return {@link CreateUploadUrlRespBO}
     */
    public CreateUploadUrlRespBO breakResume(FileMetadataInfoVo fileMetadataVo) {

        CreateUploadUrlRespBO result = new CreateUploadUrlRespBO();
        result.setParts(new ArrayList<>());
        result.setPartCount(fileMetadataVo.getPartNumber());

        // 分块数量
        Integer chunkNum = fileMetadataVo.getPartNumber();
        // 获取分块信息
        ListPartsResponse listPartsResponse = this.buildResultPart(fileMetadataVo);
        List<Part> parts = listPartsResponse.result().partList();
        if (!chunkNum.equals(parts.size())) {
            // 找到丢失的片
            boolean[] exists = new boolean[chunkNum + 1];
            // 遍历数组，标记存在的块号
            for (Part item : parts) {
                int partNumber = item.partNumber();
                exists[partNumber] = true;
            }
            // 查找丢失的块号
            List<Integer> missingNumbers = new ArrayList<>();
            for (int i = 1; i <= chunkNum; i++) {
                if (!exists[i]) {
                    missingNumbers.add(i);
                }
            }
            CreateUploadUrlReqBO bo = new CreateUploadUrlReqBO();
            // 文件md5
            bo.setFileMd5(fileMetadataVo.getFileMd5());
            // 文件名（含扩展名）
            bo.setFullFileName(fileMetadataVo.getFileName());
            // "文件长度"
            bo.setFileSize(fileMetadataVo.getFileSize());
            // 是否断点续传 0:否 1:是,默认非断点续传
            bo.setIsSequel(Boolean.TRUE);
            // 丢失的块号-断点续传时必传
            bo.setMissPartNum(missingNumbers);

            if(missingNumbers.size() != chunkNum){
                // 任务id，任务id可能会失效
                bo.setUploadId(fileMetadataVo.getUploadTaskId());
            }

            // 存储桶
            bo.setStorageBucket(fileMetadataVo.getStorageBucket());
            // 存储路径
            bo.setStoragePath(fileMetadataVo.getStoragePath());
            // 文件id
            bo.setFileKey(fileMetadataVo.getFileKey());
            result = this.createUploadUrl(bo);

        }

        return result;

    }

    /**
     * 合并分片
     */
    public CompleteResultVo completeMultipartUpload(FileMetadataInfoVo meteData, List<String> partMd5List) {

        CompleteResultVo completeResultVo = new CompleteResultVo();

        // 获取所有的分片信息
        ListPartsResponse listMultipart = this.buildResultPart(meteData);

        List<Integer> missingNumbers =new ArrayList<>();

        // 分块数量
        Integer chunkNum = meteData.getPartNumber();

        if(partMd5List==null || chunkNum != partMd5List.size()){
            throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(),"文件分块MD5校验码数量与实际分块不一致");
        }

        // 校验文件完整性
        for (int i = 1; i <= chunkNum; i++) {
            boolean findPart = false;
            for (Part part : listMultipart.result().partList()) {
                if(part.partNumber() == i && part.etag().equals(partMd5List.get(i-1))){
                    findPart = true;
                }
            }
            if(!findPart){
                missingNumbers.add(i);
            }
        }

        if(CollUtil.isNotEmpty(missingNumbers)){
            CreateUploadUrlReqBO bo = new CreateUploadUrlReqBO();
            // 文件md5
            bo.setFileMd5(meteData.getFileMd5());
            // 文件名（含扩展名）
            bo.setFullFileName(meteData.getFileName());
            // "文件长度"
            bo.setFileSize(meteData.getFileSize());
            // 是否断点续传 0:否 1:是,默认非断点续传
            bo.setIsSequel(Boolean.TRUE);
            // 丢失的块号-断点续传时必传
            bo.setMissPartNum(missingNumbers);
            if(missingNumbers.size() != chunkNum){
                // 任务id，任务id可能会失效
                bo.setUploadId(meteData.getUploadTaskId());
            }
            // 存储桶
            bo.setStorageBucket(meteData.getStorageBucket());
            // 存储路径
            bo.setStoragePath(meteData.getStoragePath());
            // 文件id
            bo.setFileKey(meteData.getFileKey());
            CreateUploadUrlRespBO createUploadUrlRespBO = this.createUploadUrl(bo);

            completeResultVo.setIsComplete(false);
            completeResultVo.setUploadTaskId(createUploadUrlRespBO.getUploadTaskId());
            completeResultVo.setPartList(createUploadUrlRespBO.getParts());
        }else{
            // 合并分块
            ObjectWriteResponse writeResponse = minioRepository.completeMultipartUpload(MultipartUploadCreateDTO.builder()
                    .bucketName(meteData.getStorageBucket())
                    .uploadId(meteData.getUploadTaskId())
                    .objectName(listMultipart.object())
                    .parts(listMultipart.result().partList().toArray(new Part[]{}))
                    .build());
            completeResultVo.setIsComplete(null!=writeResponse);
            completeResultVo.setPartList(new ArrayList<>());
        }

        return completeResultVo;
    }

    /**
     * 获取分片信息
     *
     * @param meteData 文件元数据信息
     * @return {@link ListPartsResponse}    分片任务信息
     */
    private ListPartsResponse buildResultPart(FileMetadataInfoVo meteData){
        String objectName = MinioPlusCommonUtil.getObjectName(meteData.getFileMd5());

        try {
            // 获取所有的分片信息
            return minioRepository.listMultipart(MultipartUploadCreateDTO.builder()
                    .bucketName(meteData.getStorageBucket())
                    .objectName(objectName)
                    .maxParts(meteData.getPartNumber())
                    .uploadId(meteData.getUploadTaskId())
                    .partNumberMarker(0)
                    .build());
        }catch (MinioPlusBusinessException e){
            log.error("获取分片信息失败，partList返回空",e);
            MultipartUploadCreateDTO multipartUploadCreateDTO = MultipartUploadCreateDTO.builder()
                    .bucketName(meteData.getStorageBucket())
                    .objectName(objectName)
                    .maxParts(meteData.getPartNumber())
                    .uploadId(meteData.getUploadTaskId())
                    .partNumberMarker(0)
                    .build();

            ListPartsResultCopy listPartsResult = new ListPartsResultCopy();

            return new ListPartsResponse(null,multipartUploadCreateDTO.getBucketName(),multipartUploadCreateDTO.getRegion(),multipartUploadCreateDTO.getObjectName(),listPartsResult);
        }

    }

    public CreateUploadUrlRespBO createUploadUrl(CreateUploadUrlReqBO bo) {
        // 计算分块数量
        Integer chunkNum = this.computeChunkNum(bo.getFileSize());
        // 分块信息集合
        List<FileCheckResultVo.Part> partList = new ArrayList<>();
        // 存储桶
        String bucketName;
        // 存储路径
        String storagePath;
        // 文件key
        String fileKey;
        // 额外的查询参数字段
        Map<String, String> queryParams = new HashMap<>(2);
        // 断点续传
        if (Boolean.TRUE.equals(bo.getIsSequel()) && CollUtil.isNotEmpty(bo.getMissPartNum()) && CharSequenceUtil.isNotBlank(bo.getUploadId())) {
            // 断点续传需要使用已创建的任务信息构建分片信息
            MultipartUploadCreateDTO uploadCreateDTO = MultipartUploadCreateDTO.builder()
                    .bucketName(bo.getStorageBucket())
                    .objectName(MinioPlusCommonUtil.getObjectName(bo.getFileMd5()))
                    .build();
            // 存储桶
            bucketName = uploadCreateDTO.getBucketName();
            // 存储路径
            storagePath = uploadCreateDTO.getObjectName();
            // 文件key
            fileKey = bo.getFileKey();
            queryParams.put(UPLOAD_ID, bo.getUploadId());
            uploadCreateDTO.setUploadId(bo.getUploadId());
            // 开始位置
            long start = (long) (bo.getMissPartNum().get(0) - 1) * properties.getPart().getSize();
            for (int partNumber : bo.getMissPartNum()) {
                FileCheckResultVo.Part part = this.buildResultPart(uploadCreateDTO, queryParams, bo.getFileSize(), start, partNumber);
                // 更改下一次的开始位置
                start = start + properties.getPart().getSize();
                partList.add(part);
            }
        } else {
            // 获取文件后缀
            String suffix = FileUtil.getSuffix(bo.getFullFileName());
            if (CharSequenceUtil.isBlank(suffix)) {
                throw new MinioPlusBusinessException(ResponseCodeEnum.FAIL.getCode(), "无法获取文件的拓展名");
            }
            // 文件key
            fileKey = IdUtil.fastSimpleUUID();
            // 存储路径
            storagePath = MinioPlusCommonUtil.getPathByDate();

            // 存储桶
            bucketName = StorageBucketEnums.getBucketByFileSuffix(suffix);
            // 创建桶
            minioRepository.createBucket(bucketName);
            // 如果是图片并开启了压缩,不需要分片,返回项目上的接口地址
            if (bucketName.equals(StorageBucketEnums.IMAGE.getCode()) && properties.getThumbnail().isEnable()) {

                FileCheckResultVo.Part part = new FileCheckResultVo.Part();
                // 图片上传时，直接使用fileKey作为uploadId
                part.setUploadId(fileKey);
                part.setUrl("/storage/upload/image/"+fileKey);
                part.setStartPosition(0L);
                part.setEndPosition(bo.getFileSize());
                partList.add(part);

                queryParams.put(UPLOAD_ID, fileKey);
            } else {
                // 创建分片请求,获取uploadId
                MultipartUploadCreateDTO uploadCreateDTO = MultipartUploadCreateDTO.builder()
                        .bucketName(bucketName)
                        .objectName(MinioPlusCommonUtil.getObjectName(bo.getFileMd5()))
                        .build();
                CreateMultipartUploadResponse createMultipartUploadResponse = minioRepository.createMultipartUpload(uploadCreateDTO);
                queryParams.put(UPLOAD_ID, createMultipartUploadResponse.result().uploadId());
                uploadCreateDTO.setUploadId(createMultipartUploadResponse.result().uploadId());
                long start = 0;
                for (Integer partNumber = 1; partNumber <= chunkNum; partNumber++) {
                    FileCheckResultVo.Part part = this.buildResultPart(uploadCreateDTO, queryParams, bo.getFileSize(), start, partNumber);
                    // 更改下一次的开始位置
                    start = start + properties.getPart().getSize();
                    partList.add(part);
                }
            }
        }
        CreateUploadUrlRespBO respBO = new CreateUploadUrlRespBO();
        CreateUploadUrlRespBO.MinioStorageBO minioStorageBO = new CreateUploadUrlRespBO.MinioStorageBO();
        // 桶名字
        minioStorageBO.setBucketName(bucketName);
        // 文件存储路径
        minioStorageBO.setStoragePath(storagePath);
        // 文件id-必填
        respBO.setFileKey(fileKey);
        // 分块数量-可选,分片后必须重新赋值 默认1
        respBO.setPartCount(chunkNum);
        // 切片上传任务id
        respBO.setUploadTaskId(queryParams.get(UPLOAD_ID));
        // minio存储信息-可选,使用minio存储引擎必填
        respBO.setMinioStorage(minioStorageBO);
        // 分片信息-必填
        respBO.setParts(partList);
        return respBO;
    }

}