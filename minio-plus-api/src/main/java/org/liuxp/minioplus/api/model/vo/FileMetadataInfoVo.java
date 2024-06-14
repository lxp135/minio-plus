package org.liuxp.minioplus.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 文件元数据信息VO
 *
 * @author contact@liuxp.me
 * @since 2023-06-26
 **/
@Getter
@Setter
@Schema(description = "文件元数据信息")
public class FileMetadataInfoVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "文件KEY")
    private String fileKey;

    @Schema(description = "文件md5")
    private String fileMd5;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "MIME类型")
    private String fileMimeType;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "文件长度")
    private Long fileSize;

    @Schema(description = "存储引擎")
    private String storageEngine;

    @Schema(description = "存储桶")
    private String storageBucket;

    @Schema(description = "存储路径")
    private String storagePath;

    @Schema(description = "minio切片任务id")
    private String uploadTaskId;

    @Schema(description = "状态 0:未完成 1:已完成")
    private Boolean isFinished;

    @Schema(description = "是否分块 0:否 1:是")
    private Boolean isPart;

    @Schema(description = "分块数量")
    private Integer partNumber;

    @Schema(description = "预览图 0:无 1:有")
    private Boolean isPreview;

    @Schema(description = "是否私有 0:否 1:是")
    private Boolean isPrivate;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "修改时间")
    private Date updateTime;

}