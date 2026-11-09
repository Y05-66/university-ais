package com.ais.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * 通用缓存工具类，封装对 CacheManager 的常用操作。
 *
 * <p>设计目的：为 Service 层提供简洁的缓存调用方式，屏蔽底层 Redis 序列化细节。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 读取缓存，miss 则查库并回填
 * List<College> list = cacheHelper.getOrComputeList("college:list", () -> collegeService.list(), 30);
 *
 * // 写入后清除缓存
 * cacheHelper.invalidate("college:list");
 * }</pre>
 */
@Slf4j
@Component
@Lazy
@RequiredArgsConstructor
public class CacheHelper {

    /** 底层缓存管理器，负责实际的 Redis 读写操作 */
    private final CacheManager cacheManager;

    /** JSON 序列化工具，用于缓存写入时的对象序列化 */
    private final ObjectMapper objectMapper;

    /**
     * 获取缓存，未命中则从数据库加载并回填。
     *
     * @param key        缓存键（不含前缀）
     * @param dbLoader   数据库加载 Lambda，在缓存 miss 时调用
     * @param ttlMinutes 缓存过期时间（分钟）
     * @return 缓存数据或数据库查询结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String key, Supplier<T> dbLoader, long ttlMinutes) {
        // 先尝试直接获取（用于简单类型）
        T cached = cacheManager.get(key);
        if (cached != null) {
            return cached;
        }
        T result = dbLoader.get();
        if (result != null) {
            cacheManager.set(key, result, java.time.Duration.ofMinutes(ttlMinutes));
        }
        return result;
    }

    /**
     * 带类型的安全缓存获取。
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String key, Class<T> type, Supplier<T> dbLoader, long ttlMinutes) {
        T cached = cacheManager.get(key, type);
        if (cached != null) {
            return cached;
        }
        T result = dbLoader.get();
        if (result != null) {
            cacheManager.set(key, result, java.time.Duration.ofMinutes(ttlMinutes));
        }
        return result;
    }

    /**
     * 获取缓存 List，未命中则从数据库加载并回填。
     *
     * @param key        缓存键（不含前缀）
     * @param dbLoader   数据库加载 Lambda，在缓存 miss 时调用
     * @param ttlMinutes 缓存过期时间（分钟）
     * @return 缓存数据或数据库查询结果
     */
    public <T> List<T> getOrComputeList(String key, Supplier<List<T>> dbLoader, long ttlMinutes) {
        List<T> cached = cacheManager.get(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<T> result = dbLoader.get();
        if (result != null && !result.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(result);
                cacheManager.set(key, result, java.time.Duration.ofMinutes(ttlMinutes));
            } catch (Exception e) {
                log.warn("缓存写入失败: {} — {}", key, e.getMessage());
            }
        }
        return result != null ? result : List.of();
    }

    /**
     * 删除指定缓存，使缓存失效。
     * 通常在数据修改操作（增/删/改）后调用。
     *
     * @param key 要删除的缓存键（不含前缀）
     */
    public void invalidate(String key) {
        cacheManager.invalidate(key);
    }

    /**
     * 批量删除多个缓存。
     *
     * @param keys 要删除的缓存键（不含前缀）
     */
    public void invalidateAll(String... keys) {
        cacheManager.invalidateAll(keys);
    }
}
