package org.liuxp.minioplus.extension.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 文件完成入参DTO
 *
 * @author contact@liuxp.me
 * @since 2023/8/26
 */
@Getter
@Setter
@ToString
@Schema(description = "文件完成入参DTO")
public class FileCompleteDTO {

    @ApiModelProperty(value = "文件md5", required = true)
    private List<String> partMd5List;

}