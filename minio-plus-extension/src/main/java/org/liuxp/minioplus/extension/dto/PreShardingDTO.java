package org.liuxp.minioplus.extension.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("文件预分片入参DTO")
public class PreShardingDTO {

    @ApiModelProperty(value = "文件长度", required = true)
    private Long fileSize;

}