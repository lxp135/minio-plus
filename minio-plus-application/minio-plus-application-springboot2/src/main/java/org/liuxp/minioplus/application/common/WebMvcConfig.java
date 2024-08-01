package org.liuxp.minioplus.application.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SpringMVC配置
 *
 * @author contact@liuxp.me
 * @since 2024/06/11
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginUserInterceptor loginUserInterceptor;

    public WebMvcConfig(LoginUserInterceptor loginUserInterceptor) {
        this.loginUserInterceptor = loginUserInterceptor;
    }

    /**
     * 前置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录用户
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/storage/**");
    }
}