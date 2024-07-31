package org.liuxp.minioplus.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "文件KEY")
    private String fileKey;

    @Schema(description = "文件md5")
    private String fileMd5;

    @Schema(description = "存储桶")
    private String bucket;

    @Schema(description = "是否私有 false:否 true:是")
    private Boolean isPrivate;

    @Schema(description = "状态 false:未完成 true:已完成")
    private Boolean isFinished;

    @Schema(description = "创建人")
    private String createUser;

}