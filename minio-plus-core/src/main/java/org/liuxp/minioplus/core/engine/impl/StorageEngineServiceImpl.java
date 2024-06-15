package org.liuxp.minioplus.core.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.api.model.bo.CreateUploadUrlReqBO;
import org.liuxp.minioplus.api.model.bo.CreateUploadUrlRespBO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoDTO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoSaveDTO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoUpdateDTO;
import org.liuxp.minioplus.api.model.vo.CompleteResultVo;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.api.model.vo.FileMetadataInfoVo;
import org.liuxp.minioplus.common.config.MinioPlusProperties;
import org.liuxp.minioplus.common.enums.MinioPlusErrorCode;
import org.liuxp.minioplus.common.enums.StorageBucketEnums;
import org.liuxp.minioplus.common.exception.MinioPlusException;
import org.liuxp.minioplus.core.common.utils.CommonUtil;
import org.liuxp.minioplus.core.engine.StorageEngineService;
import org.liuxp.minioplus.core.repository.MetadataRepository;
import org.liuxp.minioplus.s3.def.ListParts;
import org.liuxp.minioplus.s3.def.MinioS3Client;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 存储引擎Service接口实现类
 *
 * @author contact@liuxp.me
 * @since 2024/05/30
 */
@Service
@Slf4j
public class StorageEngineServiceImpl implements StorageEngineService {

    @Resource
    MetadataRepository metadataRepository;

    @Resource
    MinioPlusProperties properties;

    @Resource
    MinioS3Client minioS3Client;

    /**
     * 上传任务初始化
     * <p>
     * 1.当前用户或其他用户上传过，且已完成，秒传，新增文件元数据
     * 2.当前用户上传过，未完成，断点续传
     * 3.其他用户上传过，未完成，断点续传，新增文件元数据
     * 4.从未上传过，下发上传链接，新增文件元数据
     *
     * @param fileMd5      文件md5
     * @param fullFileName 文件名（含扩展名）
     * @param fileSize     文件长度
     * @param isPrivate    是否私有 false:否 true:是
     * @return {@link FileCheckResultVo}
     */
    @Override
    public FileCheckResultVo init(String fileMd5, String fullFileName, long fileSize, Boolean isPrivate, String userId) {

        // 根据MD5查询文件是否已上传过
        FileMetadataInfoDTO searchDTO = new FileMetadataInfoDTO();
        searchDTO.setFileMd5(fileMd5);
        List<FileMetadataInfoVo> list = metadataRepository.list(searchDTO);

        FileMetadataInfoSaveDTO saveDTO = new FileMetadataInfoSaveDTO();
        CreateUploadUrlReqBO bo = new CreateUploadUrlReqBO();

        if (CollUtil.isNotEmpty(list)) {
            //  1.当前用户或其他用户上传过，且已完成，秒传，新增文件元数据
            for (FileMetadataInfoVo fileMetadataInfoVo : list) {
                if (fileMetadataInfoVo.getIsFinished()) {
                    // 秒传
                    saveDTO.setFileKey(IdUtil.fastSimpleUUID()); // 文件KEY
                    saveDTO.setFileMd5(fileMd5); // 文件md5
                    saveDTO.setFileName(fullFileName); // 文件名
                    saveDTO.setFileMimeType(fileMetadataInfoVo.getFileMimeType()); // MIME类型
                    saveDTO.setFileSuffix(fileMetadataInfoVo.getFileSuffix()); // 文件后缀
                    saveDTO.setFileSize(fileMetadataInfoVo.getFileSize()); // 文件长度
                    saveDTO.setStorageBucket(fileMetadataInfoVo.getStorageBucket()); // 存储桶
                    saveDTO.setStoragePath(fileMetadataInfoVo.getStoragePath()); // 存储桶路径
                    saveDTO.setIsFinished(fileMetadataInfoVo.getIsFinished()); // 状态 0:未完成 1:已完成
                    saveDTO.setIsPreview(fileMetadataInfoVo.getIsPreview()); // 预览图 0:无 1:有
                    saveDTO.setIsPrivate(isPrivate); // 是否私有 0:否 1:是
                    saveDTO.setCreateUser(userId); // 创建人
                    saveDTO.setUpdateUser(userId); // 修改人

                    FileMetadataInfoVo metadataInfoVo = metadataRepository.save(saveDTO);
                    return this.buildResult(metadataInfoVo, new ArrayList<>(1), 0, Boolean.TRUE);
                }
            }

            // 取得当前用户上传任务
            Optional<FileMetadataInfoVo> userUploaded = list.stream().filter(item -> userId.equals(item.getCreateUser())).findFirst();

            FileMetadataInfoVo uploadingMetadata;

            boolean isSelf = userUploaded.isPresent();

            if (!isSelf) {
                uploadingMetadata = list.stream()
                        .filter(FileMetadataInfoVo::getIsFinished)
                        .findAny()
                        .orElseGet(() -> list.stream()
                                .filter(item -> !item.getIsFinished()).findFirst().get());
            } else {
                uploadingMetadata = userUploaded.get();
            }

            // 上传过未完成-断点续传
            bo.setIsSequel(Boolean.TRUE);
            CreateUploadUrlRespBO respBO = this.breakResume(uploadingMetadata);

            if (!isSelf) {
                // 3.其他用户上传过，未完成，断点续传，新增文件元数据
                // 插入自己的元数据
                BeanUtils.copyProperties(uploadingMetadata, saveDTO);
                saveDTO.setFileName(fullFileName);
                saveDTO.setCreateUser(userId);
                saveDTO.setIsPrivate(isPrivate);
                saveDTO.setUploadTaskId(respBO.getUploadTaskId());
                FileMetadataInfoVo metadataInfoVo = metadataRepository.save(saveDTO);

                return this.buildResult(metadataInfoVo, respBO.getParts(), respBO.getPartCount(), Boolean.FALSE);
            } else {
                // 2.当前用户上传过，未完成，断点续传
                if (CollUtil.isNotEmpty(respBO.getParts()) && !respBO.getUploadTaskId().equals(uploadingMetadata.getUploadTaskId())) {
                    // 原uploadTaskId失效时，同时更新原记录
                    uploadingMetadata.setUploadTaskId(respBO.getUploadTaskId());
                    FileMetadataInfoUpdateDTO updateDTO = new FileMetadataInfoUpdateDTO();
                    updateDTO.setId(uploadingMetadata.getId());
                    updateDTO.setUploadTaskId(uploadingMetadata.getUploadTaskId());
                    updateDTO.setUpdateUser(userId);
                    metadataRepository.update(updateDTO);
                }

                return this.buildResult(uploadingMetadata, respBO.getParts(), respBO.getPartCount(), Boolean.FALSE);
            }
        } else {
            // 4.从未上传过，下发上传链接，新增文件元数据

            bo.setFileMd5(fileMd5);
            bo.setFileSize(fileSize);
            bo.setFullFileName(fullFileName);

            CreateUploadUrlRespBO createUploadUrlRespBO = this.createUploadUrl(bo);

            FileMetadataInfoVo metadataInfo = saveMetadataInfo(saveDTO, createUploadUrlRespBO, fileMd5, fullFileName, fileSize, isPrivate, userId);

            return this.buildResult(metadataInfo, createUploadUrlRespBO.getParts(), createUploadUrlRespBO.getPartCount(), Boolean.FALSE);
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
     * @param saveDTO               元数据保存实体类
     * @param createUploadUrlRespBO 上传链接参数
     * @param fileMd5               文件md5
     * @param fullFileName          文件名（含扩展名）
     * @param fileSize              文件长度
     * @param isPrivate             是否私有 false:否 true:是
     * @param userId                用户编号
     * @return {@link FileMetadataInfoVo}
     */
    private FileMetadataInfoVo saveMetadataInfo(FileMetadataInfoSaveDTO saveDTO, CreateUploadUrlRespBO createUploadUrlRespBO,
                                                String fileMd5, String fullFileName, long fileSize, boolean isPrivate, String userId) {
        // 保存文件元数据
        String suffix = FileUtil.getSuffix(fullFileName);
        // 文件KEY
        saveDTO.setFileKey(createUploadUrlRespBO.getFileKey());
        // 文件md5
        saveDTO.setFileMd5(fileMd5);
        // 文件名
        saveDTO.setFileName(fullFileName);
        // MIME类型
        saveDTO.setFileMimeType(FileUtil.getMimeType(fullFileName));
        // 文件后缀
        saveDTO.setFileSuffix(suffix);
        // 文件长度
        saveDTO.setFileSize(fileSize);
        // 存储桶
        saveDTO.setStorageBucket(createUploadUrlRespBO.getBucketName());
        // 存储路径
        saveDTO.setStoragePath(createUploadUrlRespBO.getStoragePath());
        // 上传任务id
        saveDTO.setUploadTaskId(createUploadUrlRespBO.getUploadTaskId());
        // 状态 0:未完成 1:已完成
        saveDTO.setIsFinished(Boolean.FALSE);
        // 是否分片
        saveDTO.setIsPart(createUploadUrlRespBO.getPartCount() > 1);
        // 分片数量
        saveDTO.setPartNumber(createUploadUrlRespBO.getPartCount());
        // 预览图 0:无 1:有
        saveDTO.setIsPreview(Boolean.FALSE);
        // 是否私有 0:否 1:是
        saveDTO.setIsPrivate(isPrivate);
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
    public CompleteResultVo complete(String fileKey, List<String> partMd5List, String userId) {

        CompleteResultVo completeResultVo = new CompleteResultVo();

        FileMetadataInfoDTO searchDto = new FileMetadataInfoDTO();
        // 用户id
        searchDto.setCreateUser(userId);
        // 文件key
        searchDto.setFileKey(fileKey);

        FileMetadataInfoVo metadata = metadataRepository.one(searchDto);

        if (metadata == null) {
            log.error(fileKey + MinioPlusErrorCode.FILE_EXIST_FAILED.getMessage());
            throw new MinioPlusException(MinioPlusErrorCode.FILE_EXIST_FAILED.getCode(), fileKey + MinioPlusErrorCode.FILE_EXIST_FAILED.getMessage());
        }

        if (Boolean.TRUE.equals(metadata.getIsFinished())) {
            // 如果文件已上传完成，直接返回true，不进行合并
            completeResultVo.setIsComplete(true);
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
            if (CollUtil.isNotEmpty(others)) {
                for (FileMetadataInfoVo other : others) {
                    updateDTO = new FileMetadataInfoUpdateDTO();
                    updateDTO.setId(other.getId());
                    updateDTO.setIsFinished(Boolean.TRUE);
                    updateDTO.setUpdateUser(metadata.getUpdateUser());
                    metadataRepository.update(updateDTO);
                }
            }
        } else {
            if (!metadata.getUploadTaskId().equals(completeResultVo.getUploadTaskId())) {
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
    public String download(String fileKey, String userId) {

        FileMetadataInfoVo metadata = getFileMetadataInfo(fileKey, userId);

        try {
            // 文件权限校验，元数据为空或者当前登录用户不是文件所有者时抛出异常
            this.authentication(metadata, fileKey, userId);

            return minioS3Client.getDownloadUrl(metadata.getFileName(), metadata.getFileMimeType(), metadata.getStorageBucket(), metadata.getStoragePath() + "/" + metadata.getFileMd5());
        } catch (Exception e) {
            // 打印日志
            log.error(e.getMessage(), e);
            // 任何异常，统一返回给前端文件不存在
            throw new MinioPlusException(MinioPlusErrorCode.FILE_EXIST_FAILED);
        }
    }

    @Override
    public String image(String fileKey, String userId) {

        FileMetadataInfoVo metadata = getFileMetadataInfo(fileKey, userId);

        try {
            // 文件权限校验，元数据为空或者当前登录用户不是文件所有者时抛出异常
            this.authentication(metadata, fileKey, userId);

            return minioS3Client.getPreviewUrl(metadata.getFileMimeType(), metadata.getStorageBucket(), metadata.getStoragePath() + "/" + metadata.getFileMd5());

        } catch (Exception e) {
            // 打印日志
            log.error(e.getMessage(), e);
            // 任何异常，统一返回给前端文件不存在
            throw new MinioPlusException(MinioPlusErrorCode.FILE_EXIST_FAILED);
        }
    }

    @Override
    public String preview(String fileKey, String userId) {

        FileMetadataInfoVo metadata = getFileMetadataInfo(fileKey, userId);

        try {
            // 文件权限校验，元数据为空或者当前登录用户不是文件所有者时抛出异常
            this.authentication(metadata, fileKey, userId);
            // 生成缩略图
            generatePreviewImage(metadata);
            // 创建图片预览地址
            return minioS3Client.getPreviewUrl(metadata.getFileMimeType(), StorageBucketEnums.IMAGE_PREVIEW.getCode(), metadata.getStoragePath() + "/" + metadata.getFileMd5());
        } catch (Exception e) {
            // 打印日志
            log.error(e.getMessage(), e);
            // 任何异常，统一返回给前端文件不存在
            throw new MinioPlusException(MinioPlusErrorCode.FILE_EXIST_FAILED);
        }
    }

    /**
     * 文件元数据
     *
     * @param metadata 文件元数据
     * @author <a href="mailto:tianxiang.deng@foxmail.com">BaldHead</a>
     * @since 2024-06-14 14:44:52
     */
    private void generatePreviewImage(FileMetadataInfoVo metadata) {
        try {
            if (Boolean.FALSE.equals(metadata.getIsPreview())) {
                // 获取原图的bytes
                byte[] imageOriginBytes = minioS3Client.getObject(StorageBucketEnums.IMAGE.getCode(), metadata.getStoragePath() + "/" + metadata.getFileMd5());
                // 压缩缩略图
                ByteArrayOutputStream largeImage = CommonUtil.resizeImage(new ByteArrayInputStream(imageOriginBytes), properties.getThumbnail().getSize());
                byte[] largeImageBytes = largeImage.toByteArray();
                minioS3Client.putObject(StorageBucketEnums.IMAGE_PREVIEW.getCode(), CommonUtil.getObjectName(metadata.getFileMd5()), new ByteArrayInputStream(largeImageBytes), largeImageBytes.length, metadata.getFileMimeType());
                metadata.setIsPreview(Boolean.TRUE);
                FileMetadataInfoUpdateDTO fileMetadataInfoUpdateDTO = BeanUtil.copyProperties(metadata, FileMetadataInfoUpdateDTO.class);
                metadataRepository.update(fileMetadataInfoUpdateDTO);
            }
        } catch (Exception e) {
            // 打印日志
            log.error(e.getMessage(), e);
            // 缩略图生成失败
            throw new MinioPlusException(MinioPlusErrorCode.FILE_PREVIEW_WRITE_FAILED);
        }
    }

    @Override
    public Boolean createFile(FileMetadataInfoSaveDTO saveDTO, byte[] fileBytes) {
        // 写入文件
        minioS3Client.putObject(saveDTO.getStorageBucket(), CommonUtil.getObjectName(saveDTO.getFileMd5()), new ByteArrayInputStream(fileBytes), saveDTO.getFileSize(), saveDTO.getFileMimeType());
        return true;


    }

    @Override
    public Pair<FileMetadataInfoVo, byte[]> read(String fileKey) {

        // 查询文件元数据
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileKey(fileKey);
        FileMetadataInfoVo fileMetadataInfoVo = metadataRepository.one(fileMetadataInfo);

        if (null == fileMetadataInfoVo) {
            return null;
        }

        // 读取流
        byte[] fileBytes = minioS3Client.getObject(fileMetadataInfoVo.getStorageBucket(), fileMetadataInfoVo.getStoragePath() + "/" + fileMetadataInfoVo.getFileMd5());

        return Pair.of(fileMetadataInfoVo, fileBytes);
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

    private void remove(FileMetadataInfoVo metadata) {
        // 删除元数据信息
        metadataRepository.remove(metadata.getId());

        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileMd5(metadata.getFileMd5());
        List<FileMetadataInfoVo> metadataList = metadataRepository.list(fileMetadataInfo);

        if (CollUtil.isEmpty(metadataList)) {
            // 当不存在任何该MD5值的文件元数据时，删除物理文件
            minioS3Client.removeObject(metadata.getStorageBucket(), metadata.getStoragePath() + "/" + metadata.getFileMd5());
            if (Boolean.TRUE.equals(metadata.getIsPreview())) {
                // 当存在缩略图时，同步删除缩略图
                minioS3Client.removeObject(StorageBucketEnums.IMAGE_PREVIEW.getCode(), metadata.getStoragePath() + "/" + metadata.getFileMd5());
            }
        }
    }

    /**
     * 根据用户取得文件元数据信息
     * 当userId匹配时直接返回，不匹配时检查是否存在公有元数据
     *
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
     * @param fileKey  文件key
     * @param userId   用户主键
     */
    private void authentication(FileMetadataInfoVo metadata, String fileKey, String userId) {
        if (null == metadata) {
            throw new MinioPlusException(MinioPlusErrorCode.FILE_EXIST_FAILED.getCode(), fileKey + MinioPlusErrorCode.FILE_EXIST_FAILED.getMessage());
        }

        // 元数据信息存在，判断权限
        if (Boolean.TRUE.equals(metadata.getIsPrivate()) && !userId.equals(metadata.getCreateUser())) {
            throw new MinioPlusException(MinioPlusErrorCode.FILE_PERMISSION_CHECK_FAILED.getCode(), fileKey + "用户" + userId + MinioPlusErrorCode.FILE_PERMISSION_CHECK_FAILED.getMessage());
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
     * @param bucketName 桶名称
     * @param objectName 对象名称（含路径）
     * @param uploadId   上传任务编号
     * @param fileSize   文件大小
     * @param start      开始位置
     * @param partNumber 块号
     * @return {@link FileCheckResultVo.Part}
     */
    private FileCheckResultVo.Part buildResultPart(String bucketName, String objectName, String uploadId, Long fileSize, long start, Integer partNumber) {
        // 计算起始位置
        long end = Math.min(start + properties.getPart().getSize(), fileSize);
        String uploadUrl = minioS3Client.getUploadObjectUrl(bucketName, objectName, uploadId, String.valueOf(partNumber));
        FileCheckResultVo.Part part = new FileCheckResultVo.Part();
        part.setUploadId(uploadId);
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
     * @param fileMetadataVo 文件元数据信息
     * @return CreateUploadUrlRespBO 分片结果
     */
    public CreateUploadUrlRespBO breakResume(FileMetadataInfoVo fileMetadataVo) {

        CreateUploadUrlRespBO result = new CreateUploadUrlRespBO();
        result.setParts(new ArrayList<>());
        result.setPartCount(fileMetadataVo.getPartNumber());

        // 分块数量
        Integer chunkNum = fileMetadataVo.getPartNumber();
        // 获取分块信息
        ListParts listParts = this.buildResultPart(fileMetadataVo);
        List<ListParts.Part> parts = listParts.getPartList();
        if (!chunkNum.equals(parts.size())) {
            // 找到丢失的片
            boolean[] exists = new boolean[chunkNum + 1];
            // 遍历数组，标记存在的块号
            for (ListParts.Part item : parts) {
                int partNumber = item.getPartNumber();
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

            if (missingNumbers.size() != chunkNum) {
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
     *
     * @param metadataInfo 文件元数据信息
     * @param partMd5List  分片集合
     * @return 合并结果
     */
    public CompleteResultVo completeMultipartUpload(FileMetadataInfoVo metadataInfo, List<String> partMd5List) {

        CompleteResultVo completeResultVo = new CompleteResultVo();

        // 获取所有的分片信息
        ListParts listParts = this.buildResultPart(metadataInfo);

        List<Integer> missingNumbers = new ArrayList<>();

        // 分块数量
        Integer chunkNum = metadataInfo.getPartNumber();

        if (partMd5List == null || chunkNum != partMd5List.size()) {
            throw new MinioPlusException(MinioPlusErrorCode.FILE_PART_NUM_CHECK_FAILED);
        }

        // 校验文件完整性
        for (int i = 1; i <= chunkNum; i++) {
            boolean findPart = false;
            for (ListParts.Part part : listParts.getPartList()) {
                if (part.getPartNumber() == i && CharSequenceUtil.equalsIgnoreCase(part.getEtag(), partMd5List.get(i - 1))) {
                    findPart = true;
                }
            }
            if (!findPart) {
                missingNumbers.add(i);
            }
        }

        if (CollUtil.isNotEmpty(missingNumbers)) {
            CreateUploadUrlReqBO bo = new CreateUploadUrlReqBO();
            // 文件md5
            bo.setFileMd5(metadataInfo.getFileMd5());
            // 文件名（含扩展名）
            bo.setFullFileName(metadataInfo.getFileName());
            // "文件长度"
            bo.setFileSize(metadataInfo.getFileSize());
            // 是否断点续传 0:否 1:是,默认非断点续传
            bo.setIsSequel(Boolean.TRUE);
            // 丢失的块号-断点续传时必传
            bo.setMissPartNum(missingNumbers);
            if (missingNumbers.size() != chunkNum) {
                // 任务id，任务id可能会失效
                bo.setUploadId(metadataInfo.getUploadTaskId());
            }
            // 存储桶
            bo.setStorageBucket(metadataInfo.getStorageBucket());
            // 存储路径
            bo.setStoragePath(metadataInfo.getStoragePath());
            // 文件id
            bo.setFileKey(metadataInfo.getFileKey());
            CreateUploadUrlRespBO createUploadUrlRespBO = this.createUploadUrl(bo);

            completeResultVo.setIsComplete(false);
            completeResultVo.setUploadTaskId(createUploadUrlRespBO.getUploadTaskId());
            completeResultVo.setPartList(createUploadUrlRespBO.getParts());
        } else {
            // 合并分块
            boolean writeResponse = minioS3Client.completeMultipartUpload(metadataInfo.getStorageBucket()
                    , listParts.getObjectName()
                    , metadataInfo.getUploadTaskId()
                    , listParts.getPartList()
            );
            completeResultVo.setIsComplete(writeResponse);
            completeResultVo.setPartList(new ArrayList<>());
        }

        return completeResultVo;
    }

    /**
     * 获取分片信息
     *
     * @param metadataInfo 文件元数据信息
     * @return {@link ListParts}    分片任务信息
     */
    private ListParts buildResultPart(FileMetadataInfoVo metadataInfo) {
        String objectName = CommonUtil.getObjectName(metadataInfo.getFileMd5());
        // 获取所有的分片信息
        return minioS3Client.listParts(metadataInfo.getStorageBucket(), objectName, metadataInfo.getPartNumber(), metadataInfo.getUploadTaskId());
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
        // 上传任务编号
        String uploadId;
        // 断点续传
        if (Boolean.TRUE.equals(bo.getIsSequel()) && CollUtil.isNotEmpty(bo.getMissPartNum()) && CharSequenceUtil.isNotBlank(bo.getUploadId())) {
            // 断点续传需要使用已创建的任务信息构建分片信息
            // 存储桶
            bucketName = bo.getStorageBucket();
            // 存储路径
            storagePath = CommonUtil.getObjectName(bo.getFileMd5());
            // 文件key
            fileKey = bo.getFileKey();
            uploadId = bo.getUploadId();
            // 开始位置
            long start = (long) (bo.getMissPartNum().get(0) - 1) * properties.getPart().getSize();
            for (int partNumber : bo.getMissPartNum()) {
                FileCheckResultVo.Part part = this.buildResultPart(bucketName, storagePath, uploadId, bo.getFileSize(), start, partNumber);
                // 更改下一次的开始位置
                start = start + properties.getPart().getSize();
                partList.add(part);
            }
        } else {
            // 获取文件后缀
            String suffix = FileUtil.getSuffix(bo.getFullFileName());
            if (CharSequenceUtil.isBlank(suffix)) {
                throw new MinioPlusException(MinioPlusErrorCode.FILE_SUFFIX_GET_FAILED);
            }
            // 文件key
            fileKey = IdUtil.fastSimpleUUID();
            // 存储路径
            storagePath = CommonUtil.getPathByDate();

            // 存储桶
            bucketName = StorageBucketEnums.getBucketByFileSuffix(suffix);
            // 创建桶
            minioS3Client.makeBucket(bucketName);

            // 创建分片请求,获取uploadId
            uploadId = minioS3Client.createMultipartUpload(bucketName, CommonUtil.getObjectName(bo.getFileMd5()));
            long start = 0;
            for (Integer partNumber = 1; partNumber <= chunkNum; partNumber++) {
                FileCheckResultVo.Part part = this.buildResultPart(bucketName, CommonUtil.getObjectName(bo.getFileMd5()), uploadId, bo.getFileSize(), start, partNumber);
                // 更改下一次的开始位置
                start = start + properties.getPart().getSize();
                partList.add(part);
            }
        }
        CreateUploadUrlRespBO respBO = new CreateUploadUrlRespBO();
        // 桶名字
        respBO.setBucketName(bucketName);
        // 文件存储路径
        respBO.setStoragePath(storagePath);
        // 文件id-必填
        respBO.setFileKey(fileKey);
        // 分块数量-可选,分片后必须重新赋值 默认1
        respBO.setPartCount(chunkNum);
        // 切片上传任务id
        respBO.setUploadTaskId(uploadId);
        // 分片信息-必填
        respBO.setParts(partList);
        return respBO;
    }

}
