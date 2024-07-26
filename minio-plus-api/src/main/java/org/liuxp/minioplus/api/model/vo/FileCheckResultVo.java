package org.liuxp.minioplus.api.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件初始化结果
 *
 * @author contact@liuxp.me
 * @since 2024-06-26
 **/
@Getter
@Setter
@ApiModel("文件初始化结果")
public class FileCheckResultVo {
    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Long id;
    /**
     * 文件KEY
     */
    @ApiModelProperty("文件KEY")
    private String fileKey;
    /**
     * 文件md5
     */
    @ApiModelProperty("文件md5")
    private String fileMd5;
    /**
     * 文件名
     */
    @ApiModelProperty("文件名")
    private String fileName;

    /**
     * MIME类型
     */
    @ApiModelProperty("MIME类型")
    private String fileMimeType;
    /**
     * 文件后缀
     */
    @ApiModelProperty("文件后缀")
    private String fileSuffix;
    /**
     * 文件长度
     */
    @ApiModelProperty("文件长度")
    private Long fileSize;
    /**
     * 是否秒传
     */
    @ApiModelProperty("是否秒传")
    private Boolean isDone;
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
         * minio的上传id
         */
        @ApiModelProperty("minio的上传id")
        private String uploadId;
        /**
         * 上传地址
         */
        @ApiModelProperty("上传地址")
        private String url;
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