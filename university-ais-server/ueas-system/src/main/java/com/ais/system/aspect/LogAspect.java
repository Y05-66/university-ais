package com.ais.system.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.ais.system.entity.SysLog;
import com.ais.system.mapper.SysLogMapper;
import com.ais.security.util.SaTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面。
 *
 * <p>自动记录 Controller 层的方法调用日志，包括操作人、操作类型、请求参数、执行耗时等。
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper logMapper;

    /**
     * 拦截 Controller 层的公共方法。
     *
     * <p>记录操作日志，异常时记录错误信息。
     */
    @Around("execution(* com.ais..controller.*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysLog log = new SysLog();

        try {
            // 设置操作信息
            log.setUserId(SaTokenUtil.getCurrentUserId());
            log.setUsername(SaTokenUtil.getCurrentUsername());
            log.setOperation(point.getSignature().getName());
            log.setMethod(point.getSignature().toString());
            log.setParams(buildParams(point));
            log.setIp(getClientIp());
            log.setStatus(1);
            log.setCreateTime(LocalDateTime.now());

            // 执行目标方法
            Object result = point.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.setDuration((int) duration);
            logMapper.insert(log);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.setDuration((int) duration);
            log.setStatus(0);
            log.setErrorMsg(e.getMessage());
            logMapper.insert(log);
            throw e;
        }
    }

    /**
     * 获取客户端真实 IP。
     */
    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 构建请求参数字符串。
     */
    private String buildParams(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg != null && arg.getClass().getName().contains("HttpServletRequest") == false
                && arg.getClass().getName().contains("HttpServletResponse") == false) {
                sb.append(arg.toString()).append(",");
            }
        }
        return sb.toString();
    }
}
