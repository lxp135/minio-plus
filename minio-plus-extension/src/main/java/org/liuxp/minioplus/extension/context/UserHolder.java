package org.liuxp.minioplus.extension.context;

/**
 * 用户信息助手
 * 当使用 minio-plus-extension 模块中的 controller 时，存取登录用户编号
 * @author contact@liuxp.me
 * @since  2024/06/11
 */
public class UserHolder {

    /**
     * 登录用户编号
     */
    private static final ThreadLocal<String> userThreadLocal = new ThreadLocal<>();

    /**
     * 获取登录用户编号
     * @return 登录用户编号
     */
    public static String get() {
        return userThreadLocal.get();
    }

    /**
     * 设置登录用户编号
     * @param userId 登录用户编号
     */
    public static void set(String userId) {
        userThreadLocal.set(userId);
    }

    /**
     * 清除登录用户编号
     */
    public static void clean() {
        userThreadLocal.remove();
    }

}