package com.ais.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Knife4j API 文档配置。
 *
 * <p>配置 Swagger/OpenAPI 文档信息，访问 http://localhost:8080/doc.html 查看接口文档。
 */
@Configuration
@EnableKnife4j
public class Knife4jConfig {

    /**
     * 自定义 OpenAPI 配置。
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("大学教务管理系统 API")
                        .version("1.0")
                        .description("大学教务管理系统 REST API 文档")
                        .contact(new Contact().name("AIS Team")));
    }
}
