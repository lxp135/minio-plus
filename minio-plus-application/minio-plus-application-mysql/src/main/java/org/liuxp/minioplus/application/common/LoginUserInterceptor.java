package org.liuxp.minioplus.application.common;

import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.extension.context.UserHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录用户拦截器
 *
 * @author contact@liuxp.me
 * @since  2024/06/11
 */
@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    /**
     * 处理登录用户信息
     *
     * @param request  请求
     * @param response 返回
     * @param handler  要执行的处理程序
     * @return 是否继续执行下一个拦截器
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        String userId = request.getHeader("Authorization");
        UserHolder.set(userId);
        return true;
    }

    /**
     * 清理资源
     *
     * @param request   请求
     * @param response  返回
     * @param handler   要执行的处理程序
     * @param exception 异常信息
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception exception) {
        log.debug("Project ThreadLocal 清理之前:{}", UserHolder.get());
        UserHolder.clean();
        log.debug("Project ThreadLocal 清理之之后:{}", UserHolder.get());
    }

}
