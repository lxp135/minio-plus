package org.liuxp.minioplus.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件元数据信息保存入参
 *
 * @author contact@liuxp.me
 * @since 2023-06-26
 **/
@Getter
@Setter
@ToString
@Schema(description = "文件元数据信息保存入参")
public class FileMetadataInfoSaveDTO {
    /**
     * 文件KEY
     */
    @Schema(description = "文件KEY")
    private String fileKey;
    /**
     * 文件md5
     */
    @Schema(description = "文件md5")
    private String fileMd5;
    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;
    /**
     * MIME类型
     */
    @Schema(description = "MIME类型")
    private String fileMimeType;
    /**
     * 文件后缀
     */
    @Schema(description = "文件后缀")
    private String fileSuffix;
    /**
     * 文件长度
     */
    @Schema(description = "文件长度")
    private Long fileSize;
    /**
     * 存储桶
     */
    @Schema(description = "存储桶")
    private String storageBucket;
    /**
     * 存储路径
     */
    @Schema(description = "存储路径")
    private String storagePath;

    /**
     * 上传任务id,用于合并切片
     */
    @Schema(description = "上传任务id,用于合并切片")
    private String uploadTaskId;
    /**
     * 状态 0:未完成 1:已完成
     */
    @Schema(description = "状态 false:未完成 true:已完成")
    private Boolean isFinished;
    /**
     * 是否分块 0:否 1:是
     */
    @Schema(description = "是否分块 false:否 true:是")
    private Boolean isPart;
    /**
     * 分块数量
     */
    @Schema(description = "分块数量")
    private Integer partNumber;
    /**
     * 预览图 0:无 1:有
     */
    @Schema(description = "预览图 false:无 true:有")
    private Boolean isPreview;
    /**
     * 是否私有 0:否 1:是
     */
    @Schema(description = "是否私有 false:否 true:是")
    private Boolean isPrivate;
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateUser;

}