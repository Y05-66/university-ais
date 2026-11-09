package com.ais.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * 统一 Redis 缓存管理器。
 *
 * <p>封装 Redis 缓存操作，为业务层提供统一的缓存访问接口。
 * 所有缓存键以 {@code ueas:cache:} 为前缀，便于统一管理和清理。
 */
@Slf4j
@Component
public class CacheManager {

    public static final String PREFIX = "ueas:cache:";
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    public static final Duration AUTH_TTL = Duration.ofHours(5);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        String json = redisTemplate.opsForValue().get(PREFIX + key);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return (T) objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            log.warn("缓存读取失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(PREFIX + key);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("缓存读取失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL);
    }

    public void set(String key, Object value, Duration ttl) {
        if (value == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(PREFIX + key, json, ttl);
        } catch (Exception e) {
            log.warn("缓存写入失败: key={}, error={}", key, e.getMessage());
        }
    }

    public void invalidate(String key) {
        redisTemplate.delete(PREFIX + key);
    }

    public void invalidateAll(String... keys) {
        for (String key : keys) {
            redisTemplate.delete(PREFIX + key);
        }
    }

    public <T> List<T> getOrCompute(String key, Supplier<List<T>> supplier, Duration ttl,
                                     TypeReference<List<T>> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + key);
            if (json != null && !json.isBlank()) {
                return objectMapper.readValue(json, typeRef);
            }
        } catch (Exception e) {
            log.warn("缓存读取失败: key={}, error={}", key, e.getMessage());
        }
        List<T> result = supplier.get();
        if (result != null && !result.isEmpty()) {
            try {
                String serialized = objectMapper.writeValueAsString(result);
                redisTemplate.opsForValue().set(PREFIX + key, serialized, ttl);
            } catch (Exception e) {
                log.warn("缓存写入失败: key={}, error={}", key, e.getMessage());
            }
        }
        return result;
    }
}
