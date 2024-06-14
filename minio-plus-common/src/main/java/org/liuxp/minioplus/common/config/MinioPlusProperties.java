package org.liuxp.minioplus.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinioPlus配置类
 *
 * @author contact@liuxp.me
 * @since 2024/05/22
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minioplus")
public class MinioPlusProperties {

    /**
     * MinIO引擎地址
     */
    private String backend;

    /**
     * 存储引擎key
     */
    private String key;

    /**
     * 存储引擎密钥
     */
    private String secret;

    /**
     * 浏览器访问地址，文件、图片上传下载访问地址代理，如果minio被nginx代理，需要配置这个参数为代理后的前端访问地址
     * 可选参数
     */
    private String browserUrl;

    /**
     * 上传预签名URL有效期，单位为分钟
     * 可选参数，默认值为60分钟
     */
    private Integer uploadExpiry = 60;

    /**
     * 下载和预览预签名URL有效期，单位为分钟
     * 可选参数，默认值为10分钟
     */
    private Integer downloadExpiry = 10;

    /**
     * 分块配置
     */
    private Part part = new Part();

    /**
     * 缩略图配置
     */
    private Thumbnail thumbnail = new Thumbnail();

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Part {

        /**
         * 是否开启分块策略，默认为true
         */
        private boolean enable = true;

        /**
         * 分块大小，配置单位为byte，默认为5242880(5MB)
         */
        private int size = 5242880;

        /**
         * 分块上传时建议并发数，默认为3
         */
        private int iis = 3;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Thumbnail {

        /**
         * 是否开启缩略图。默认为true
         *
         * @since 2024-06-14 15:47:46
         * feature 缩略图懒生成
         */
        @Deprecated
        private boolean enable = true;

        /**
         * 缩略图尺寸，默认为300
         */
        private int size = 300;

    }

}