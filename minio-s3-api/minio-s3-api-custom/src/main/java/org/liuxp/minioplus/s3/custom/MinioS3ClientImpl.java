package org.liuxp.minioplus.s3.custom;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.common.config.MinioPlusProperties;
import org.liuxp.minioplus.s3.def.ListParts;
import org.liuxp.minioplus.s3.def.MinioS3Client;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Repository
public class MinioS3ClientImpl implements MinioS3Client {

    @Resource
    private MinioPlusProperties properties;

    @Override
    public Boolean bucketExists(String bucketName) {

        // 取得当前时间
        ZonedDateTime date = ZonedDateTime.now();

        // HTTP请求
        HttpResponse httpResponse = S3Request.request(properties.getKey(),properties.getSecret(),properties.getBackend()
                ,properties.getBackend()+"/"+bucketName,"","/"+bucketName, Method.HEAD.name(),date,0);

        return httpResponse.isOk();

    }

    @Override
    public void makeBucket(String bucketName) {

    }

    @Override
    public String createMultipartUpload(String bucketName, String objectName) {
        return null;
    }

    @Override
    public Boolean completeMultipartUpload(String bucketName, String objectName, String uploadId, List<ListParts.Part> parts) {
        return null;
    }

    @Override
    public ListParts listParts(String bucketName, String objectName, Integer maxParts, String uploadId) {

        // 获取失败时，拼一个空的返回值
        return null;
    }

    @Override
    public String getUploadObjectUrl(String bucketName, String objectName, String uploadId, String partNumber) {
        return null;
    }

    @Override
    public String getDownloadUrl(String fileName, String contentType, String bucketName, String objectName) {
        return null;
    }

    @Override
    public String getPreviewUrl(String contentType, String bucketName, String objectName) {
        return null;
    }

    @Override
    public Boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType) {
        return null;
    }

    @Override
    public byte[] getObject(String bucketName, String objectName) {
        return new byte[0];
    }

    @Override
    public void removeObject(String bucketName, String objectName) {

    }
}
