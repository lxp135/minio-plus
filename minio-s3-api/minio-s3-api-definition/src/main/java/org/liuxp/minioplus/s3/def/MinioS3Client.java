package org.liuxp.minioplus.s3.def;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * MinIO S3文件存储引擎接口定义
 *
 * @author contact@liuxp.me
 * @since  2024/06/03
 */
public interface MinioS3Client {

    /**
     * 判断存储桶是否存在
     * @param bucketName 桶名称
     * @return 是否存在
     */
    Boolean bucketExists(String bucketName);

    /**
     * 创建桶
     * @param bucketName 桶名称
     */
    void makeBucket(String bucketName);

    /**
     * 创建上传任务
     * @param bucketName 桶名称
     * @param objectName 对象名称（含路径）
     * @return UploadId 上传任务编号
     */
    String createMultipartUpload(String bucketName, String objectName);

    /**
     * 合并分片
     * @param bucketName 桶名称
     * @param objectName 对象名称（含路径）
     * @param uploadId 上传任务编号
     * @param parts 分片信息 partNumber 和 etag
     * @return 是否成功
     */
    Boolean completeMultipartUpload(String bucketName, String objectName, String uploadId, List<ListParts.Part> parts);

    /**
     * 获取分片信息列表
     * @param bucketName 桶名称
     * @param objectName 对象名称（含路径）
     * @param maxParts 分片数量
     * @param uploadId 上传任务编号
     * @return 分片信息
     */
    ListParts listParts(String bucketName,String objectName,Integer maxParts,String uploadId);

    /**
     * 获得对象和分片上传链接
     * @param bucketName  桶名称
     * @param objectName  对象名称（含路径）
     * @param uploadId  上传任务编号
     * @param partNumber 分片序号
     * @return {@link String}
     */
    String getUploadObjectUrl(String bucketName, String objectName, String uploadId, String partNumber);

    /**
     * 取得下载链接
     * @param fileName 文件全名含扩展名
     * @param contentType 数据类型
     * @param bucketName 桶名称
     * @param objectName 对象名称含路径
     * @return 下载地址
     */
    String getDownloadUrl(String fileName, String contentType, String bucketName, String objectName);

    /**
     * 取得图片预览链接
     * @param contentType 数据类型
     * @param bucketName 桶名称
     * @param objectName 对象名称含路径
     * @return 图片预览链接
     */
    String getPreviewUrl(String contentType, String bucketName, String objectName);

    /**
     * 写入文件
     * @param bucketName 桶名称
     * @param objectName 对象名称含路径
     * @param stream 文件流
     * @param size 文件长度
     * @param contentType 文件类型
     * @return 是否成功
     */
    Boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType);

    /**
     * 读取文件
     * @param bucketName 桶名称
     * @param objectName 对象名称含路径
     * @return 文件流
     */
    byte[] getObject(String bucketName, String objectName);

    /**
     * 删除文件
     * @param bucketName 桶名称
     * @param objectName 对象名称含路径
     */
    void removeObject(String bucketName, String objectName);

}