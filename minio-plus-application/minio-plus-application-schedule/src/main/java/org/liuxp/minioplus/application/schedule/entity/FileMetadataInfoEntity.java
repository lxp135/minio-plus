package org.liuxp.minioplus.application.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 文件元数据信息表Entity
 * @author contact@liuxp.me
 * @since  2024-05-22
 **/
@Getter
@Setter
@ToString
@TableName(value = "file_metadata_info")
public class FileMetadataInfoEntity {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "文件KEY")
    @TableField(value = "file_key")
    private String fileKey;
    
    @Schema(description = "文件md5")
    @TableField(value = "file_md5")
    private String fileMd5;
    
    @Schema(description = "文件名")
    @TableField(value = "file_name")
    private String fileName;
    
    @Schema(description = "MIME类型")
    @TableField(value = "file_mime_type")
    private String fileMimeType;
    
    @Schema(description = "文件后缀")
    @TableField(value = "file_suffix")
    private String fileSuffix;
    
    @Schema(description = "文件长度")
    @TableField(value = "file_size")
    private Long fileSize;

    @Schema(description = "预览图 0:无 1:有")
    @TableField(value = "is_preview")
    private Boolean isPreview;

    @Schema(description = "是否私有 0:否 1:是")
    @TableField(value = "is_private")
    private Boolean isPrivate;

    @Schema(description = "存储桶")
    @TableField(value = "bucket")
    private String storageBucket;

    @Schema(description = "存储桶路径")
    @TableField(value = "bucket_path")
    private String storagePath;

    @Schema(description = "上传任务id,用于合并切片")
    @TableField(value = "upload_id")
    private String uploadTaskId;

    @Schema(description = "状态 0:未完成 1:已完成")
    @TableField(value = "is_finished")
    private Boolean isFinished;

    @Schema(description = "是否分块 0:否 1:是")
    @TableField(value = "is_part")
    private Boolean isPart;

    @Schema(description = "分块数量")
    @TableField(value = "part_number")
    private Integer partNumber;

    @Schema(description = "创建人")
    @TableField("create_user")
    private String createUser;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @Schema(description = "修改人")
    @TableField("update_user")
    private String updateUser;

    @Schema(description = "修改时间")
    @TableField("update_time")
    private Date updateTime;

}