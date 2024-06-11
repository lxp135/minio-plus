package org.liuxp.minioplus.api.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件元数据查询实体类
 *
 * @author contact@liuxp.me
 * @since 2023/6/25
 */
@Getter
@Setter
@ToString
public class FileMetadataInfoDTO {

    @ApiModelProperty("文件KEY")
    private String fileKey;

    @ApiModelProperty("文件md5")
    private String fileMd5;

    @ApiModelProperty("存储桶")
    private String bucket;

    @ApiModelProperty("是否私有 false:否 true:是")
    private Boolean isPrivate;

    @ApiModelProperty("状态 false:未完成 true:已完成")
    private Boolean isFinished;

    @ApiModelProperty("创建人")
    private String createUser;

}