package com.ais.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 异常处理器。
 *
 * <p>专门处理 Sa-Token 抛出的 {@link NotLoginException}，
 * 将其转换为 HTTP 401 响应，使前端能正确触发登出和跳转登录页。
 */
@RestControllerAdvice
public class SaTokenExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SaTokenExceptionHandler.class);

    /**
     * 处理 Sa-Token 未登录异常。
     *
     * <p>当 token 无效、已过期或被注销时，Sa-Token 会抛出此异常。
     * 返回 HTTP 401 而非 500，确保前端 axios 拦截器能正确识别并执行登出流程。
     *
     * @param e 未登录异常
     * @return HTTP 401 响应
     */
    @ExceptionHandler(NotLoginException.class)
    public R<?> handleNotLoginException(NotLoginException e) {
        log.debug("Sa-Token 未登录: type={}, message={}", e.getType(), e.getMessage());
        return R.fail(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage());
    }
}
