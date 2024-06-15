package org.liuxp.minioplus.common.enums;

/**
 * MinIO Plus 错误码
 * @author contact@liuxp.me
 * @since  2024/05/26
 */
public enum MinioPlusErrorCode {

    /**
     * 默认异常
     */
    FAIL(1000, "操作失败"),

    /**
     * MinIO Plus 异常
     */
    FILE_EXIST_FAILED(1001,"文件不存在"),
    FILE_PERMISSION_CHECK_FAILED(1002,"没有访问权限"),
    FILE_PART_NUM_CHECK_FAILED(1003,"文件分块MD5校验码数量与实际分块不一致"),
    FILE_SUFFIX_GET_FAILED(1004,"无法获取文件的拓展名"),
    FILE_BYTES_FAILED(1005,"文件流不能为空"),
    FILE_UPLOAD_FAILED(1006,"文件上传失败"),
    FILE_PREVIEW_WRITE_FAILED(1007,"缩略图生成失败"),

    /**
     * MinIO 异常
     */
    BUCKET_EXISTS_FAILED(2001, "桶检查失败"),
    MAKE_BUCKET_FAILED(2002, "桶创建失败"),
    CREATE_MULTIPART_UPLOAD_FAILED(2003, "获取上传编号失败"),
    COMPLETE_MULTIPART_FAILED(2004, "合并分片失败"),
    LIST_PARTS_FAILED(2005, "查询分片失败"),
    CREATE_UPLOAD_URL_FAILED(2006, "获取对象上传URL失败"),
    CREATE_DOWNLOAD_URL_FAILED(2007, "获取对象下载URL失败"),
    CREATE_PREVIEW_URL_FAILED(2008, "获取预对象预览URL失败"),
    WRITE_FAILED(2009, "文件写入失败"),
    READ_FAILED(2010, "文件读取失败"),
    DELETE_FAILED(2011, "删除失败");

    /**
     * 错误编码
     */
    private final Integer code;
    /**
     * 错误提示信息
     */
    private final String message;

    /**
     * 构造方法
     *
     * @param code 错误编码
     * @param message 错误提示信息
     */
    MinioPlusErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 取得错误编码
     * @return 错误编码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 取得错误提示信息
     * @return 错误提示信息
     */
    public String getMessage() {
        return message;
    }

}
