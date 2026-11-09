package com.ais.system.service;

/**
 * 系统配置服务。
 *
 * <p>管理系统级配置项，使用 Redis 存储，支持高并发读写。
 */
public interface SysConfigService {
    /**
     * 获取配置项值。
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String get(String key, String defaultValue);

    /**
     * 设置配置项值。
     *
     * @param key 配置键
     * @param value 配置值
     */
    void set(String key, String value);

    /**
     * 获取布尔配置项。
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * 设置布尔配置项。
     *
     * @param key 配置键
     * @param value 布尔值
     */
    void setBoolean(String key, boolean value);
}
