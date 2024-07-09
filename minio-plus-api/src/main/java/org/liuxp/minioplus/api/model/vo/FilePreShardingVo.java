package org.liuxp.minioplus.api.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件预分片结果
 * @author contact@liuxp.me
 * @since 2024-07-09
 **/
@Getter
@Setter
@ApiModel("文件预分片结果")
public class FilePreShardingVo {

    /**
     * 文件长度
     */
    @ApiModelProperty("文件长度")
    private Long fileSize;

    /**
     * 分块数量
     */
    @ApiModelProperty("分块数量")
    private Integer partCount;

    /**
     * 分块大小
     */
    @ApiModelProperty("分块大小")
    private Integer partSize;

    /**
     * 分块信息
     */
    @ApiModelProperty("分块信息")
    private List<Part> partList = new ArrayList<>();

    @Getter
    @Setter
    public static class Part {

        /**
         * 开始位置
         */
        @ApiModelProperty("开始位置")
        private Long startPosition;
        /**
         * 结束位置
         */
        @ApiModelProperty("结束位置")
        private Long endPosition;

    }

}