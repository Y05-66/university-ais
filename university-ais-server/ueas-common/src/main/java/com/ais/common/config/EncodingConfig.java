package com.ais.common.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

/**
 * 字符编码配置类。
 *
 * <p>配置全局 UTF-8 字符编码过滤器，确保所有请求和响应的字符编码统一为 UTF-8。
 * 同时自定义 Tomcat 连接器配置，放宽 URL 中的特殊字符限制。
 */
@Configuration
public class EncodingConfig {

    /**
     * 注册字符编码过滤器，强制使用 UTF-8 编码。
     *
     * @return 配置好的字符编码过滤器
     */
    @Bean
    @Order(1)
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding(StandardCharsets.UTF_8.name());
        filter.setForceEncoding(true);
        return filter;
    }

    /**
     * 自定义 Tomcat 服务器配置，放宽 URL 查询参数中的特殊字符限制。
     *
     * @return Tomcat 服务器定制器
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            // 允许 URL 中包含 []{} 等特殊字符
            connector.setProperty("relaxedQueryChars", "[]{}");
            connector.getScheme(); // 强制初始化连接器
        });
    }
}
