package com.ais.common.exception;

import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>统一捕获 Controller 层抛出的异常，转换为标准的 {@link R} 响应格式返回给前端。
 * 避免异常信息直接暴露给客户端，同时提供友好的错误提示。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（BizException）。
     * 业务异常由 Service 层主动抛出，如参数校验失败、资源不存在等。
     *
     * @param e 业务异常对象
     * @return 封装了错误码和消息的响应
     */
    @ExceptionHandler(BizException.class)
    public R<?> handleBizException(BizException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 注解触发）。
     * 当前端提交的数据不符合 DTO 上的校验规则时抛出。
     *
     * @param e 参数校验异常
     * @return 封装了字段错误信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidException(MethodArgumentNotValidException e) {
        // 收集所有字段错误，拼接成可读的错误消息
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return R.fail(ResultCode.PARAM_INVALID.getCode(), msg);
    }

    /**
     * 处理数据库唯一键冲突异常。
     * 根据错误信息判断具体是哪个字段重复，返回友好的中文提示。
     *
     * @param e 数据库完整性约束异常
     * @return 友好的错误提示
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String msg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        log.warn("数据完整性冲突: {}", msg);

        if (msg != null) {
            // 学号重复
            if (msg.contains("uk_student_no") || msg.contains("学生")) {
                return R.fail(ResultCode.STUDENT_NO_DUPLICATE.getCode(), ResultCode.STUDENT_NO_DUPLICATE.getMessage());
            }
            // 工号重复
            if (msg.contains("uk_teacher_no") || msg.contains("教师")) {
                return R.fail(ResultCode.TEACHER_NO_DUPLICATE.getCode(), ResultCode.TEACHER_NO_DUPLICATE.getMessage());
            }
            // 用户名重复
            if (msg.contains("uk_username") || msg.contains("username")) {
                return R.fail(ResultCode.USERNAME_DUPLICATE.getCode(), ResultCode.USERNAME_DUPLICATE.getMessage());
            }
            // 课程编号重复
            if (msg.contains("uk_course_no") || msg.contains("课程")) {
                return R.fail(ResultCode.COURSE_NO_DUPLICATE.getCode(), ResultCode.COURSE_NO_DUPLICATE.getMessage());
            }
            // 代码重复（学院/专业/班级）
            if (msg.contains("uk_code") || msg.contains("代码")) {
                return R.fail(ResultCode.CODE_DUPLICATE.getCode(), ResultCode.CODE_DUPLICATE.getMessage());
            }
        }
        return R.fail(ResultCode.INTERNAL_ERROR.getCode(), "数据保存失败，请检查输入是否有重复");
    }

    /**
     * 处理其他未捕获的系统异常。
     * 兜底处理器，防止异常信息直接暴露给前端。
     *
     * @param e 系统异常
     * @return 统一的系统错误响应
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return R.fail(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMessage());
    }
}
