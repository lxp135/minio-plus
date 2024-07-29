package org.liuxp.minioplus.s3.official;

import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Part;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 继承MinioClient重写
 * @author contact@liuxp.me
 * @since  2024/5/22
 */
public class CustomMinioClient extends MinioAsyncClient {


    public CustomMinioClient(MinioAsyncClient client) {
        super(client);
    }

    /**
     * 创建分片上传请求
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param headers          消息头
     * @param extraQueryParams 额外查询参数
     */
    @Override
    public CreateMultipartUploadResponse createMultipartUpload(String bucketName, String region, String objectName, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        return super.createMultipartUpload(bucketName, region, objectName, headers, extraQueryParams);
    }

    /**
     * 完成分片上传，执行合并文件
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param parts            分片
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    @Override
    public ObjectWriteResponse completeMultipartUpload(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        return super.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }


    /**
     * 查询分片数据
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    @Override
    public ListPartsResponse listParts(String bucketName, String region, String objectName, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        return super.listParts(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }
}
