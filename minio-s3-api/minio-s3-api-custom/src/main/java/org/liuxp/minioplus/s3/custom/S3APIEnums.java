package org.liuxp.minioplus.s3.custom;

/**
 * S3 API 接口定义
 * @author contact@liuxp.me
 * @since  2024/06/07
 */
public enum S3APIEnums {

    BUCKET_EXISTS("检查桶是否存在", "http://localhost:9000/document","/document","HEAD");

    private final String name;
    private final String url;
    private final String path;
    private final String method;

    S3APIEnums(String name,String url,String path,String method) {
        this.name = name;
        this.url = url;
        this.path = path;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}
