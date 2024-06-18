package org.liuxp.minioplus.application.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 接口文档配置
 * @author : contact@liuxp.me
 * @since 2024/6/18
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    @Bean(value = "dockerBean")
    public Docket dockerBean() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.liuxp.minioplus.extension.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("MinIO Plus API 文档")
                .contact(new Contact("刘小平", "https://liuxp.me", "contact@liuxp.me"))
                .version("1.0.0")
                .description("MinIO-Plus 是一个 MinIO 的二次封装与增强工具，在 MinIO 的基础上只做增强，不侵入 MinIO 代码，只为简化开发、提高效率而生。成为 MinIO 在项目中落地的润滑剂。")
                .license("The Apache License, Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .build();
    }
}
