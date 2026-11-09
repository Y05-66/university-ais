package com.ais.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 配置类。
 *
 * <p>注册 {@link StringRedisTemplate} Bean，供业务代码直接使用 Redis 进行字符串读写。
 * 所有缓存操作通过 {@link CacheManager} 和 {@link CacheHelper} 封装，本类仅提供底层连接。
 */
@Configuration
public class RedisConfig {

    /**
     * 创建 StringRedisTemplate Bean。
     *
     * <p>使用 String 序列化器，Key 和 Value 均以 String 形式存储。
     * 适合存储 JSON 字符串格式的缓存数据。
     *
     * @param connectionFactory Redis 连接工厂（由 Spring Boot 自动配置）
     * @return StringRedisTemplate 实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
