package org.liuxp.minioplus.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.liuxp.minioplus.api.StorageService;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoDTO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoSaveDTO;
import org.liuxp.minioplus.api.model.vo.CompleteResultVo;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.api.model.vo.FileMetadataInfoVo;
import org.liuxp.minioplus.api.model.vo.FilePreShardingVo;
import org.liuxp.minioplus.common.config.MinioPlusProperties;
import org.liuxp.minioplus.common.enums.MinioPlusErrorCode;
import org.liuxp.minioplus.common.enums.StorageBucketEnums;
import org.liuxp.minioplus.common.exception.MinioPlusException;
import org.liuxp.minioplus.core.common.utils.CommonUtil;
import org.liuxp.minioplus.core.common.utils.ContentTypeUtil;
import org.liuxp.minioplus.core.engine.StorageEngineService;
import org.liuxp.minioplus.core.repository.MetadataRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 存储组件Service层公共方法实现类
 * @author contact@liuxp.me
 * @since  2023/06/26
 */
@Service
public class StorageServiceImpl implements StorageService {

    /**
     * 存储引擎Service接口定义
     */
    @Resource
    StorageEngineService storageEngineService;

    /**
     * 文件元数据服务接口定义
     */
    @Resource
    MetadataRepository fileMetadataRepository;

    /**
     * MinioPlus配置信息注入类
     */
    @Resource
    MinioPlusProperties properties;

    @Override
    public FilePreShardingVo sharding(long fileSize) {

        // 计算分块数量
        Integer chunkNum = storageEngineService.computeChunkNum(fileSize);

        List<FilePreShardingVo.Part> partList = new ArrayList<>();

        long start = 0;
        for (int partNumber = 1; partNumber <= chunkNum; partNumber++) {

            long end = Math.min(start + properties.getPart().getSize(), fileSize);

            FilePreShardingVo.Part part = new FilePreShardingVo.Part();
            // 开始位置
            part.setStartPosition(start);
            // 结束位置
            part.setEndPosition(end);

            // 更改下一次的开始位置
            start = start + properties.getPart().getSize();
            partList.add(part);
        }

        FilePreShardingVo filePreShardingVo = new FilePreShardingVo();
        filePreShardingVo.setFileSize(fileSize);
        filePreShardingVo.setPartCount(chunkNum);
        filePreShardingVo.setPartSize(properties.getPart().getSize());
        filePreShardingVo.setPartList(partList);

        return filePreShardingVo;
    }

    @Override
    public FileCheckResultVo init(String fileMd5, String fullFileName, long fileSize, Boolean isPrivate, String userId) {

        // isPrivate 为空时，设置为 false
        isPrivate = isPrivate!=null && isPrivate ;

        FileCheckResultVo resultVo =  storageEngineService.init(fileMd5,fullFileName,fileSize,isPrivate,userId);

        if(resultVo!=null){
            for (FileCheckResultVo.Part part : resultVo.getPartList()) {
                part.setUrl(remakeUrl(part.getUrl()));
            }
        }

        return resultVo;
    }

    @Override
    public CompleteResultVo complete(String fileKey, List<String> partMd5List, String userId) {
        CompleteResultVo completeResultVo =  storageEngineService.complete(fileKey,partMd5List,userId);

        if(completeResultVo!=null){
            for (FileCheckResultVo.Part part : completeResultVo.getPartList()) {
                part.setUrl(remakeUrl(part.getUrl()));
            }
        }

        return completeResultVo;
    }

    @Override
    public String download(String fileKey, String userId) {
        return storageEngineService.download(fileKey,userId);
    }

    @Override
    public String image(String fileKey, String userId) {
        return storageEngineService.image(fileKey,userId);
    }

    @Override
    public String preview(String fileKey, String userId) {
        return storageEngineService.preview(fileKey,userId);
    }

    @Override
    public FileMetadataInfoVo one(String key) {

        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileKey(key);
        return fileMetadataRepository.one(fileMetadataInfo);

    }

    @Override
    public List<FileMetadataInfoVo> list(FileMetadataInfoDTO fileMetadataInfo) {
        // 列表查询，取得全部符合条件的数据
        return fileMetadataRepository.list(fileMetadataInfo);
    }

    @Override
    public FileMetadataInfoVo createFile(String fullFileName, Boolean isPrivate, String userId, byte[] fileBytes) {

        // 组装文件保存入参
        FileMetadataInfoSaveDTO saveDTO = buildSaveDto(fullFileName, isPrivate, userId,fileBytes);

        // 查询MinIO中是否存在相同MD5值的文件
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileMd5(saveDTO.getFileMd5());
        List<FileMetadataInfoVo> alreadyFileList = fileMetadataRepository.list(fileMetadataInfo);

        boolean sameMd5 = false;

        if(CollUtil.isNotEmpty(alreadyFileList)){
            for (FileMetadataInfoVo fileMetadataInfoVo : alreadyFileList) {
                if(Boolean.TRUE.equals(fileMetadataInfoVo.getIsFinished())){
                    saveDTO.setStorageBucket(fileMetadataInfoVo.getStorageBucket());
                    saveDTO.setStoragePath(fileMetadataInfoVo.getStoragePath());
                    sameMd5 = true;
                    break;
                }
            }
        }

        if(!sameMd5){
            // 新文件时，执行写入逻辑
            storageEngineService.createFile(saveDTO, fileBytes);
        }

        return fileMetadataRepository.save(saveDTO);

    }

    @Override
    public FileMetadataInfoVo createFile(String fullFileName, Boolean isPrivate, String userId, InputStream inputStream) {
        return createFile(fullFileName, isPrivate, userId,IoUtil.readBytes(inputStream));
    }

    @Override
    public FileMetadataInfoVo createFile(String fullFileName, Boolean isPrivate, String userId, String url) {
        // 请求文件
        HttpResponse httpResponse = HttpUtil.createGet(url).execute();
        // 获得输入流
        InputStream inputStream = httpResponse.bodyStream();
        // 调用处理函数
        return createFile(fullFileName, isPrivate, userId,inputStream);
    }

    @Override
    public FileMetadataInfoVo createBigFile(String fullFileName, String md5, long fileSize,Boolean isPrivate, String userId,InputStream inputStream) {
        // 组装文件保存入参
        FileMetadataInfoSaveDTO saveDTO = buildSaveDto(fullFileName,md5,fileSize, isPrivate, userId);

        // 查询MinIO中是否存在相同MD5值的文件
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileMd5(saveDTO.getFileMd5());
        List<FileMetadataInfoVo> alreadyFileList = fileMetadataRepository.list(fileMetadataInfo);

        boolean sameMd5 = false;

        if(CollUtil.isNotEmpty(alreadyFileList)){
            for (FileMetadataInfoVo fileMetadataInfoVo : alreadyFileList) {
                if(Boolean.TRUE.equals(fileMetadataInfoVo.getIsFinished())){
                    saveDTO.setStorageBucket(fileMetadataInfoVo.getStorageBucket());
                    saveDTO.setStoragePath(fileMetadataInfoVo.getStoragePath());
                    sameMd5 = true;
                    break;
                }
            }
        }

        if(!sameMd5){
            // 新文件时，执行写入逻辑
            storageEngineService.createFile(saveDTO, inputStream);
        }

        return fileMetadataRepository.save(saveDTO);
    }

    @Override
    public Pair<FileMetadataInfoVo,byte[]> read(String fileKey) {
        return storageEngineService.read(fileKey);
    }

    @Override
    public Boolean remove(String fileKey) {
        return storageEngineService.remove(fileKey);
    }

    FileMetadataInfoSaveDTO buildSaveDto(String fullFileName, Boolean isPrivate, String userId, byte[] fileBytes){

        if(null==fileBytes){
            throw new MinioPlusException(MinioPlusErrorCode.FILE_BYTES_FAILED);
        }
        // 计算文件MD5值
        String md5 = SecureUtil.md5().digestHex(fileBytes);

        return buildSaveDto(fullFileName,md5,fileBytes.length,isPrivate,userId);
    }

    FileMetadataInfoSaveDTO buildSaveDto(String fullFileName, String md5,long fileSize,Boolean isPrivate, String userId){

        // 生成UUID作为文件KEY
        String key = IdUtil.fastSimpleUUID();
        String suffix = FileUtil.getSuffix(fullFileName);
        String fileMimeType = ContentTypeUtil.getContentType(suffix);

        // 根据文件后缀取得桶
        String storageBucket = StorageBucketEnums.getBucketByFileSuffix(suffix);

        // 取得存储路径
        String storagePath = CommonUtil.getPathByDate();

        // 是否存在缩略图
        Boolean isPreview = properties.getThumbnail().isEnable() && StorageBucketEnums.IMAGE.getCode().equals(storageBucket);

        // 创建文件元数据信息
        FileMetadataInfoSaveDTO fileMetadataInfoSaveDTO = new FileMetadataInfoSaveDTO();
        fileMetadataInfoSaveDTO.setFileKey(key);
        fileMetadataInfoSaveDTO.setFileMd5(md5);
        fileMetadataInfoSaveDTO.setFileName(fullFileName);
        fileMetadataInfoSaveDTO.setFileMimeType(fileMimeType);
        fileMetadataInfoSaveDTO.setFileSuffix(suffix);
        fileMetadataInfoSaveDTO.setFileSize(fileSize);
        fileMetadataInfoSaveDTO.setStorageBucket(storageBucket);
        fileMetadataInfoSaveDTO.setStoragePath(storagePath);
        fileMetadataInfoSaveDTO.setUploadTaskId("");
        fileMetadataInfoSaveDTO.setIsFinished(true);
        fileMetadataInfoSaveDTO.setIsPart(false);
        fileMetadataInfoSaveDTO.setPartNumber(0);
        fileMetadataInfoSaveDTO.setIsPreview(isPreview);
        fileMetadataInfoSaveDTO.setIsPrivate(isPrivate);
        fileMetadataInfoSaveDTO.setCreateUser(userId);
        fileMetadataInfoSaveDTO.setUpdateUser(userId);

        return fileMetadataInfoSaveDTO;
    }

    /**
     * 重写文件地址
     * @param url 文件地址
     * @return 重写后的文件地址
     */
    private String remakeUrl(String url){

        if(CharSequenceUtil.isNotBlank(properties.getBrowserUrl())){
            return url.replace(properties.getBackend(), properties.getBrowserUrl());
        }
        return url;
    }
}
