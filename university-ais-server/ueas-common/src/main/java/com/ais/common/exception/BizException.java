package com.ais.common.exception;

import com.ais.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常类。
 *
 * <p>Service 层在处理业务逻辑时，遇到非预期状态（如用户不存在、密码错误、资源已删除等）
 * 应抛出此异常，由 {@link com.ais.common.exception.GlobalExceptionHandler} 统一处理并返回错误响应。
 *
 * <p>使用示例：
 * <pre>{@code
 * if (user == null) {
 *     throw new BizException(ResultCode.USER_NOT_FOUND);
 * }
 * }</pre>
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    /**
     * 使用预定义结果码构造异常。
     *
     * @param resultCode 预定义的业务错误码
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用自定义消息构造异常（默认错误码 400）。
     *
     * @param message 错误消息
     */
    public BizException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 使用自定义错误码和消息构造异常。
     *
     * @param code    自定义错误码
     * @param message 错误消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
