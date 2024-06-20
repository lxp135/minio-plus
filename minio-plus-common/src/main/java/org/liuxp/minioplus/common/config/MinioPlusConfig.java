package org.liuxp.minioplus.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MinioPlusProperties自动配置类
 *
 * @author contact@liuxp.me
 */
@AutoConfiguration
@EnableConfigurationProperties(MinioPlusProperties.class)
public class MinioPlusConfig {
}