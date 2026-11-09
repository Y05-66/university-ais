package com.ais.config;

import com.ais.security.jwt.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 安全过滤器注册配置。
 *
 * <p>将 {@link JwtAuthenticationFilter} 注册为 Spring 过滤器 Bean，
 * 使每个请求经过 Sa-Token 登录校验后，能从会话中提取当前用户信息
 * 并注入到请求属性中，供 {@link com.ais.basic.util.UserContextUtil} 等组件使用。
 */
@Configuration
public class SecurityFilterConfig {

    /**
     * 注册 JwtAuthenticationFilter 到 Spring 过滤器链。
     *
     * <p>设置较低优先级（Ordered.LOWEST_PRECEDENCE），确保在 Sa-Token 拦截器之后执行，
     * 从而正确获取已登录用户信息。
     *
     * @param filter JwtAuthenticationFilter Bean
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setName("jwtAuthenticationFilter");
        return registration;
    }
}
