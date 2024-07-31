package org.liuxp.minioplus.extension.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件预分片入参DTO
 *
 * @author contact@liuxp.me
 * @since 2024/7/9
 */
@Getter
@Setter
@ToString
@Schema(description = "文件预分片入参DTO")
public class PreShardingDTO {

    @Schema(description = "文件长度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileSize;

}