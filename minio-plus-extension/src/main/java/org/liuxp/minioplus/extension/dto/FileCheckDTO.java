package org.liuxp.minioplus.extension.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件预检查DTO
 *
 * @author contact@liuxp.me
 * @since 2023/6/26
 */
@Getter
@Setter
@ToString
@Schema(description = "文件预检查入参DTO")
public class FileCheckDTO {

    @Schema(description = "文件md5", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileMd5;

    @Schema(description = "文件名（含扩展名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullFileName;

    @Schema(description = "文件长度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileSize;

    @Schema(description = "是否私有 false:否 true:是")
    private Boolean isPrivate;


}