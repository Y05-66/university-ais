package com.ais.system.config;

import cn.dev33.satoken.SaManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 数据访问配置。
 *
 * <p>配置自定义的 SaTokenDao（Redis 实现），使 Token 和 Session
 * 能够持久化到 Redis，支持分布式部署和跨会话持久化。
 */
@Configuration
public class SaTokenDaoConfig {

    @Autowired
    private SaTokenDaoRedisImpl saTokenDao;

    /**
     * 应用启动时初始化 Sa-Token 配置。
     */
    @PostConstruct
    public void init() {
        // 设置自定义的 SaTokenDao（使用 Redis 存储）
        SaManager.setSaTokenDao(saTokenDao);
    }
}
