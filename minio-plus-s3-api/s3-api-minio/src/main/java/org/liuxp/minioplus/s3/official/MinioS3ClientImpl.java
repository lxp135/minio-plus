package org.liuxp.minioplus.s3.official;

import cn.hutool.core.io.IoUtil;
import com.google.common.collect.Maps;
import io.minio.*;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.common.config.MinioPlusProperties;
import org.liuxp.minioplus.common.enums.MinioPlusErrorCode;
import org.liuxp.minioplus.common.exception.MinioPlusException;
import org.liuxp.minioplus.s3.def.ListParts;
import org.liuxp.minioplus.s3.def.MinioS3Client;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MinioS3ClientImpl implements MinioS3Client {

    /**
     * MinIO中上传编号名称
     */
    private static final String UPLOAD_ID = "uploadId";
    /**
     * 分片上传块号名称
     */
    private static final String PART_NUMBER = "partNumber";

    private final MinioPlusProperties properties;

    private CustomMinioClient minioClient = null;

    public MinioS3ClientImpl(MinioPlusProperties properties) {
        this.properties = properties;
    }

    public CustomMinioClient getClient() {

        if (null == this.minioClient) {
            MinioAsyncClient client = MinioAsyncClient.builder()
                    .endpoint(properties.getBackend())
                    .credentials(properties.getKey(), properties.getSecret())
                    .build();
            this.minioClient = new CustomMinioClient(client);
        }

        return this.minioClient;
    }

    @Override
    public Boolean bucketExists(String bucketName) {
        try {
            return this.getClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()).get();
        } catch (InsufficientDataException | InternalException | InvalidKeyException | IOException |
                 NoSuchAlgorithmException | XmlParserException | ExecutionException e) {
            log.error("{}:{}", MinioPlusErrorCode.BUCKET_EXISTS_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.BUCKET_EXISTS_FAILED);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt(); // 重新设置中断状态
            log.error("{}:{}", MinioPlusErrorCode.BUCKET_EXISTS_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.BUCKET_EXISTS_FAILED);
        }
    }

    @Override
    public void makeBucket(String bucketName) {
        boolean found = bucketExists(bucketName);
        try {
            if (!found) {
                log.info("create bucket: [{}]", bucketName);
                this.getClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("{}:{}", MinioPlusErrorCode.MAKE_BUCKET_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.MAKE_BUCKET_FAILED);
        }
    }

    @Override
    public String createMultipartUpload(String bucketName, String objectName) {
        try {
            CreateMultipartUploadResponse createMultipartUploadResponse = this.getClient().createMultipartUpload(bucketName, null, objectName, null, null);
            return createMultipartUploadResponse.result().uploadId();
        } catch (Exception e) {
            log.error("{}:{}", MinioPlusErrorCode.CREATE_MULTIPART_UPLOAD_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.CREATE_MULTIPART_UPLOAD_FAILED);
        }
    }

    @Override
    public Boolean completeMultipartUpload(String bucketName, String objectName, String uploadId, List<ListParts.Part> parts) {

        Part[] partArray = new Part[parts.size()];

        for (int i = 0; i < parts.size(); i++) {
            partArray[i] = new Part(parts.get(i).getPartNumber(), parts.get(i).getEtag());
        }

        try {
            ObjectWriteResponse objectWriteResponse = this.getClient().completeMultipartUpload(bucketName, null
                    , objectName, uploadId, partArray, null, null);
            return objectWriteResponse != null;
        } catch (Exception e) {
            log.error("{},uploadId:{},ObjectName:{},失败原因:{},", MinioPlusErrorCode.COMPLETE_MULTIPART_FAILED.getMessage(), uploadId, objectName, e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.COMPLETE_MULTIPART_FAILED);
        }
    }

    @Override
    public ListParts listParts(String bucketName, String objectName, Integer maxParts, String uploadId) {

        ListParts listParts = ListParts.build();

        try {
            ListPartsResponse listPartsResponse = this.getClient().listParts(bucketName, null, objectName, maxParts
                    , 0, uploadId, null, null);

            listParts.setBucketName(bucketName);
            listParts.setObjectName(objectName);
            listParts.setMaxParts(maxParts);
            listParts.setUploadId(uploadId);
            listParts.setPartList(new ArrayList<>());

            for (Part part : listPartsResponse.result().partList()) {
                listParts.addPart(part.partNumber(), part.etag(), part.lastModified(), part.partSize());
            }

        } catch (Exception e) {
            // 查询分片失败，打印日志，返回空的分片信息
            log.error("{}:{}", MinioPlusErrorCode.LIST_PARTS_FAILED.getMessage(), e.getMessage());
        }

        return listParts;
    }

    @Override
    public String getUploadObjectUrl(String bucketName, String objectName, String uploadId, String partNumber) {

        Map<String, String> queryParams = Maps.newHashMapWithExpectedSize(2);
        queryParams.put(UPLOAD_ID, uploadId);
        queryParams.put(PART_NUMBER, partNumber);

        try {
            return this.getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(properties.getUploadExpiry(), TimeUnit.MINUTES)
                            .extraQueryParams(queryParams)
                            .build());
        } catch (Exception e) {
            log.error("{}:{}", MinioPlusErrorCode.CREATE_UPLOAD_URL_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.CREATE_UPLOAD_URL_FAILED);
        }
    }

    @Override
    public String getDownloadUrl(String fileName, String contentType, String bucketName, String objectName) {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("response-content-disposition", "attachment;filename=\"" + fileName + "\"");
        reqParams.put("response-content-type", contentType);

        try {
            return this.getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(properties.getDownloadExpiry(), TimeUnit.MINUTES)
                            .extraQueryParams(reqParams)
                            .build());
        } catch (Exception e) {
            log.error("{}:{}", MinioPlusErrorCode.CREATE_DOWNLOAD_URL_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.CREATE_DOWNLOAD_URL_FAILED);
        }
    }

    @Override
    public String getPreviewUrl(String contentType, String bucketName, String objectName) {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("response-content-type", contentType);
        reqParams.put("response-content-disposition", "inline");

        try {
            return this.getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(properties.getDownloadExpiry(), TimeUnit.MINUTES)
                            .extraQueryParams(reqParams)
                            .build());
        } catch (Exception e) {
            log.error("{}:{}", MinioPlusErrorCode.CREATE_PREVIEW_URL_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.CREATE_PREVIEW_URL_FAILED);
        }
    }

    @Override
    public Boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType) {
        try {

            // 检查存储桶是否已经存在
            boolean isExist = this.getClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()).get();
            if (!isExist) {
                // 创建存储桶。
                this.getClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

            }

            // 使用putObject上传一个文件到存储桶中。
            this.getClient().putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(stream, size, 0L)
                    .contentType(contentType)
                    .build());

        } catch (InsufficientDataException | InternalException | InvalidKeyException | IOException |
                 NoSuchAlgorithmException | XmlParserException | ExecutionException e) {
            log.error(MinioPlusErrorCode.WRITE_FAILED.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.WRITE_FAILED);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt(); // 重新设置中断状态
            log.error("{}:{}", MinioPlusErrorCode.BUCKET_EXISTS_FAILED.getMessage(), e.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.BUCKET_EXISTS_FAILED);
        }

        return true;
    }

    @Override
    public byte[] getObject(String bucketName, String objectName) {
        // 从远程MinIO服务读取文件流
        try (InputStream inputStream = this.getClient().getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build()).get()) {
            // 文件流转换为字节码
            return IoUtil.readBytes(inputStream);
        } catch (Exception e) {
            log.error(MinioPlusErrorCode.READ_FAILED.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.READ_FAILED);
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        try {
            this.getClient().removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error(MinioPlusErrorCode.DELETE_FAILED.getMessage(), e);
            throw new MinioPlusException(MinioPlusErrorCode.DELETE_FAILED);
        }
    }
}
