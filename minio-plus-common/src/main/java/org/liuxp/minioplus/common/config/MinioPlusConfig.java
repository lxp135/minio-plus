package org.liuxp.minioplus.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MinioPlusProperties自动配置类
 * @author contact@liuxp.me
 */
@EnableConfigurationProperties(MinioPlusProperties.class)
public class MinioPlusConfig {

    @Bean
    public MinioPlusProperties tosProperties() {
        return new MinioPlusProperties();
    }

}