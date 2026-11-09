package com.ais.security.jwt;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT/Token 认证过滤器。
 *
 * <p>每个请求经过此过滤器时，尝试从 Sa-Token 会话中获取当前用户信息，
 * 并将其设置为请求属性，供后续 Controller 使用。
 *
 * <p>注意：登录接口(/auth/login)已通过 SaTokenConfig 排除拦截，
 * 此过滤器不会对其生效。
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 每个请求执行一次的身份认证逻辑。
     *
     * <p>如果当前有已登录的 Sa-Token 会话，则将用户 ID 和用户类型
     * 设置为请求属性，方便后续业务代码使用。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param chain    过滤器链
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 检查是否有已登录的 Token
            if (StpUtil.isLogin()) {
                // 从 Sa-Token session 中获取用户信息
                Long userId = StpUtil.getLoginIdAsLong();
                Object username = StpUtil.getSession().get("username");
                Object realName = StpUtil.getSession().get("realName");
                Object userType = StpUtil.getSession().get("userType");

                // 将用户信息设置为请求属性，供 Controller 使用
                request.setAttribute("currentUserId", userId);
                request.setAttribute("currentUserType", userType);
                request.setAttribute("currentUsername", username);
                request.setAttribute("currentRealName", realName);
            }
        } catch (cn.dev33.satoken.exception.NotLoginException e) {
            // 未登录，忽略（由 Sa-Token 拦截器统一处理）
        } catch (Exception e) {
            log.debug("用户未登录或token无效: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
