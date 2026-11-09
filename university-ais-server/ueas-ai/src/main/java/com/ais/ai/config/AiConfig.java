package com.ais.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AI 智能助手配置类。
 *
 * <p>提供 RestTemplate Bean，用于直接调用 OpenAI 兼容接口。
 */
@Configuration
public class AiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
