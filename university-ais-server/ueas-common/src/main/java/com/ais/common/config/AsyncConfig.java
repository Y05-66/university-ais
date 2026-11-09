package com.ais.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 异步任务配置类。
 *
 * <p>启用 Spring 异步执行和定时任务调度功能，支持使用 {@code @Async} 和 {@code @Scheduled} 注解。
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
