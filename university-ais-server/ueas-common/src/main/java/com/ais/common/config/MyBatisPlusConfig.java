package com.ais.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 *
 * <p>注册分页插件和乐观锁插件，使所有查询自动支持物理分页和乐观并发控制。
 * Service 层使用 {@code new Page<>(current, size)} 即可启用分页。
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器链。
     *
     * <p>包含分页插件和乐观锁插件：
     * <ul>
     *   <li>分页插件：针对 MySQL 数据库，自动将查询转为 LIMIT 分页 SQL</li>
     *   <li>乐观锁插件：自动处理 @Version 字段的并发更新</li>
     * </ul>
     *
     * @return 包含分页插件和乐观锁插件的拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件：针对 MySQL 数据库，自动将查询转为 LIMIT 分页 SQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件：支持 @Version 注解字段的自动版本管理
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
