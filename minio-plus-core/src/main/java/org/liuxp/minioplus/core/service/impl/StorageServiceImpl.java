package org.liuxp.minioplus.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.liuxp.minioplus.api.model.dto.FileCheckDTO;
import org.liuxp.minioplus.api.model.vo.CompleteResultVo;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.common.config.MinioPlusProperties;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoDTO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoSaveDTO;
import org.liuxp.minioplus.api.model.dto.FileSaveDTO;
import org.liuxp.minioplus.common.enums.MinioPlusErrorCode;
import org.liuxp.minioplus.common.enums.StorageBucketEnums;
import org.liuxp.minioplus.common.exception.MinioPlusException;
import org.liuxp.minioplus.core.common.utils.ContentTypeUtil;
import org.liuxp.minioplus.core.common.utils.CommonUtil;
import org.liuxp.minioplus.api.model.vo.FileMetadataInfoVo;
import org.liuxp.minioplus.core.engine.StorageEngineService;
import org.liuxp.minioplus.core.repository.MetadataRepository;
import org.liuxp.minioplus.api.StorageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
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
    public FileCheckResultVo init(FileCheckDTO dto, String userId) {
        FileCheckResultVo resultVo =  storageEngineService.init(dto,userId);

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
    public Boolean uploadImage(String fileKey, byte[] file) {
        return storageEngineService.uploadImage(fileKey,file);
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
    public FileMetadataInfoVo createFile(FileSaveDTO fileSaveDTO, byte[] fileBytes) {

        // 组装文件保存入参
        FileMetadataInfoSaveDTO saveDTO = buildSaveDto(fileSaveDTO,fileBytes);

        // 查询MinIO中是否存在相同MD5值的文件
        FileMetadataInfoDTO fileMetadataInfo = new FileMetadataInfoDTO();
        fileMetadataInfo.setFileMd5(saveDTO.getFileMd5());
        List<FileMetadataInfoVo> alreadyFileList = this.list(fileMetadataInfo);

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
            for (FileMetadataInfoVo fileMetadataInfoVo : alreadyFileList) {
                if(Boolean.TRUE.equals(fileMetadataInfoVo.getIsFinished())&&fileMetadataInfoVo.getCreateUser().equals(saveDTO.getCreateUser())){
                    // 当存在该用户上传的相同md5值文件时，直接返回，元数据不再创建
                    return fileMetadataInfoVo;
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
    public FileMetadataInfoVo createFile(FileSaveDTO fileSaveDTO, InputStream inputStream) {
        return createFile(fileSaveDTO,IoUtil.readBytes(inputStream));
    }

    @Override
    public FileMetadataInfoVo createFile(FileSaveDTO fileSaveDTO, String url) {
        // 请求文件
        HttpResponse httpResponse = HttpUtil.createGet(url).execute();
        // 获得输入流
        InputStream inputStream = httpResponse.bodyStream();
        // 调用处理函数
        return createFile(fileSaveDTO,inputStream);
    }

    @Override
    public Pair<FileMetadataInfoVo,byte[]> read(String fileKey) {
        return storageEngineService.read(fileKey);
    }

    @Override
    public Boolean remove(String fileKey) {
        return storageEngineService.remove(fileKey);
    }

    FileMetadataInfoSaveDTO buildSaveDto(FileSaveDTO fileSaveDTO, byte[] fileBytes){

        if(null==fileBytes){
            throw new MinioPlusException(MinioPlusErrorCode.FILE_BYTES_FAILED);
        }
        // 计算文件MD5值
        String md5 = SecureUtil.md5().digestHex(fileBytes);
        // 生成UUID作为文件KEY
        String key = IdUtil.fastSimpleUUID();
        String suffix = FileUtil.getSuffix(fileSaveDTO.getFullFileName());
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
        fileMetadataInfoSaveDTO.setFileName(fileSaveDTO.getFullFileName());
        fileMetadataInfoSaveDTO.setFileMimeType(fileMimeType);
        fileMetadataInfoSaveDTO.setFileSuffix(suffix);
        fileMetadataInfoSaveDTO.setFileSize((long) fileBytes.length);
        fileMetadataInfoSaveDTO.setStorageBucket(storageBucket);
        fileMetadataInfoSaveDTO.setStoragePath(storagePath);
        fileMetadataInfoSaveDTO.setUploadTaskId("");
        fileMetadataInfoSaveDTO.setIsFinished(true);
        fileMetadataInfoSaveDTO.setIsPart(false);
        fileMetadataInfoSaveDTO.setPartNumber(0);
        fileMetadataInfoSaveDTO.setIsPreview(isPreview);
        fileMetadataInfoSaveDTO.setIsPrivate(fileSaveDTO.getIsPrivate());
        fileMetadataInfoSaveDTO.setCreateUser(fileSaveDTO.getCreateUser());
        fileMetadataInfoSaveDTO.setUpdateUser(fileSaveDTO.getCreateUser());

        return fileMetadataInfoSaveDTO;
    }

    /**
     * 重写文件地址
     * @param url 文件地址
     * @return 重写后的文件地址
     */
    private String remakeUrl(String url){

        if(StrUtil.isNotBlank(properties.getBrowserUrl())){
            return url.replace(properties.getBackend(), properties.getBrowserUrl());
        }
        return url;
    }
}
