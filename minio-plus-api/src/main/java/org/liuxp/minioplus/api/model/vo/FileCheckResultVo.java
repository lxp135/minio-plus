package org.liuxp.minioplus.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "文件初始化结果")
public class FileCheckResultVo {
    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;
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
     * 是否秒传
     */
    @Schema(description = "是否秒传")
    private Boolean isDone;
    /**
     * 分块数量
     */
    @Schema(description = "分块数量")
    private Integer partCount;

    /**
     * 分块大小
     */
    @Schema(description = "分块大小")
    private Integer partSize;

    /**
     * 分块信息
     */
    @Schema(description = "分块信息")
    private List<Part> partList = new ArrayList<>();

    @Getter
    @Setter
    public static class Part {
        /**
         * minio的上传id
         */
        @Schema(description = "minio的上传id")
        private String uploadId;
        /**
         * 上传地址
         */
        @Schema(description = "上传地址")
        private String url;
        /**
         * 开始位置
         */
        @Schema(description = "开始位置")
        private Long startPosition;
        /**
         * 结束位置
         */
        @Schema(description = "结束位置")
        private Long endPosition;

    }

}