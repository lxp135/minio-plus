package org.liuxp.minioplus.core.engine;

import cn.hutool.core.lang.Pair;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoSaveDTO;
import org.liuxp.minioplus.api.model.vo.CompleteResultVo;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.api.model.vo.FileMetadataInfoVo;

import java.io.InputStream;
import java.util.List;

/**
 * 存储引擎Service接口定义
 *
 * @author contact@liuxp.me
 * @since 2023/06/26
 */
public interface StorageEngineService {

    /**
     * 计算分块的数量
     *
     * @param fileSize 文件大小
     * @return {@link Integer}
     */
    Integer computeChunkNum(Long fileSize);

    /**
     * 上传任务初始化
     * @param fileMd5 文件md5
     * @param fullFileName 文件名（含扩展名）
     * @param fileSize 文件长度
     * @param isPrivate 是否私有 false:否 true:是
     * @param userId  用户编号
     * @return {@link FileCheckResultVo}
     */
    FileCheckResultVo init(String fileMd5, String fullFileName, long fileSize, Boolean isPrivate,String userId);


    /**
     * 合并已分块的文件
     *
     * @param fileKey 文件关键
     * @param partMd5List 文件分块md5列表
     * @param userId  用户编号
     *
     * @return {@link CompleteResultVo}
     */
    CompleteResultVo complete(String fileKey, List<String> partMd5List,String userId);

    /**
     * 取得文件下载地址
     *
     * @param fileKey 文件KEY
     * @param userId  用户编号
     * @return 地址
     */
    String download(String fileKey, String userId);

    /**
     * 取得原图地址
     *
     * @param fileKey 文件KEY
     * @param userId  用户编号
     * @return 地址
     */
    String image(String fileKey, String userId);

    /**
     * 取得缩略图地址
     *
     * @param fileKey 文件KEY
     * @param userId  用户编号
     * @return 地址
     */
    String preview(String fileKey, String userId);

    /**
     * 写入文件流
     * @param saveDTO 文件元数据信息保存入参
     * @param fileBytes 文件流
     * @return 是否成功
     */
    Boolean createFile(FileMetadataInfoSaveDTO saveDTO, byte[] fileBytes);

    /**
     * 写入文件流
     * @param saveDTO 文件元数据信息保存入参
     * @param inputStream 文件流
     * @return 是否成功
     */
    Boolean createFile(FileMetadataInfoSaveDTO saveDTO, InputStream inputStream);

    /**
     * 读取文件流
     * @param fileKey 文件KEY
     * @return 文件流
     */
    Pair<FileMetadataInfoVo,byte[]> read(String fileKey);

    /**
     * 删除文件
     * @param fileKey 文件KEY
     * @return 是否成功
     */
    Boolean remove(String fileKey);

    /**
     * 删除文件
     * @param fileKey 文件KEY
     * @param userId  用户编号
     * @return 是否成功
     */
    Boolean remove(String fileKey, String userId);

}
