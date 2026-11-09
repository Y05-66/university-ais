package com.ais.system.service.impl;

import com.ais.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现类。
 *
 * <p>使用 Redis 存储系统配置项，支持高并发读写。
 * 配置项 TTL 为 24 小时，确保缓存命中率的同时支持及时更新。
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final StringRedisTemplate redisTemplate;

    /** 配置项前缀 */
    private static final String PREFIX = "sys:config:";

    /** 默认 TTL：24 小时 */
    private static final long DEFAULT_TTL_HOURS = 24;

    @Override
    public String get(String key, String defaultValue) {
        String value = redisTemplate.opsForValue().get(PREFIX + key);
        return value != null ? value : defaultValue;
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(PREFIX + key, value, DEFAULT_TTL_HOURS, java.util.concurrent.TimeUnit.HOURS);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }
}
