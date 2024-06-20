package org.liuxp.minioplus.application.common;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 接口文档配置
 *
 * @author : contact@liuxp.me
 * @since 2024/6/18
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GlobalOpenApiCustomizer dockerBean() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", RandomUtil.randomInt(0, 100));
                    tag.setExtensions(map);
                });
            }
            if (openApi.getPaths() != null) {
                openApi.addExtension("x-test123", "333");
                openApi.getPaths().addExtension("x-abb", RandomUtil.randomInt(1, 100));
            }

        };
    }


    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("MinIO Plus API 文档")
                        .version("1.0.0")
                        .description("MinIO-Plus 是一个 MinIO 的二次封装与增强工具，在 MinIO 的基础上只做增强，不侵入 MinIO 代码，只为简化开发、提高效率而生。成为 MinIO 在项目中落地的润滑剂。")
                        .termsOfService("https://liuxp.me")
                        .contact(
                                new Contact()
                                        .name("刘小平")
                                        .url("https://liuxp.me")
                                        .email("\"contact@liuxp.me\"")
                        )
                        .license(
                                new License()
                                        .name("Apache 2.0")
                                        .url("http://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                );
    }
}
