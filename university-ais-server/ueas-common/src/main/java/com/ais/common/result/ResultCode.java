package com.ais.common.result;

import lombok.Getter;

/**
 * 预定义响应结果码。
 *
 * <p>用于统一异常处理和错误码管理，避免硬编码数字码。
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "success"),

    /** 系统内部错误 */
    INTERNAL_ERROR(500, "系统内部错误"),

    /** 参数校验失败 */
    PARAM_INVALID(400, "参数校验失败"),

    /** 登录失败（统一提示，不区分账号不存在/密码错误/账号禁用） */
    LOGIN_FAILED(1006, "账号或密码错误"),

    /** 验证码已过期 */
    CAPTCHA_EXPIRED(1007, "验证码已过期，请重新获取"),

    /** 验证码错误 */
    CAPTCHA_ERROR(1008, "验证码错误"),

    /** 用户不存在 */
    USER_NOT_FOUND(1001, "用户不存在"),

    /** 密码错误 */
    PASSWORD_ERROR(1002, "密码错误"),

    /** 用户被禁用 */
    USER_LOCKED(1003, "用户已被禁用"),

    /** 资源不存在 */
    NOT_FOUND(1004, "资源不存在"),

    /** 权限不足 */
    FORBIDDEN(1005, "权限不足"),

    /** 未授权（Token 无效或已过期） */
    UNAUTHORIZED(401, "未授权"),

    /** 成绩已发布，不可修改 */
    SCORE_ALREADY_PUBLISHED(2001, "成绩已发布，不可修改"),

    /** 成绩已审核，不可修改 */
    SCORE_ALREADY_APPROVED(2002, "成绩已审核，不可修改"),

    /** 学号重复 */
    STUDENT_NO_DUPLICATE(3001, "学号已存在"),

    /** 工号重复 */
    TEACHER_NO_DUPLICATE(3002, "工号已存在"),

    /** 用户名重复 */
    USERNAME_DUPLICATE(3003, "用户名已存在"),

    /** 课程编号重复 */
    COURSE_NO_DUPLICATE(3004, "课程编号已存在"),

    /** 代码重复（学院/专业/班级） */
    CODE_DUPLICATE(3005, "代码已存在"),

    /** 资源已被使用，无法删除 */
    RESOURCE_IN_USE(3006, "该资源已被关联数据使用，无法删除"),

    /** 选课冲突：时间冲突 */
    COURSE_TIME_CONFLICT(4001, "选课时间冲突，请选择其他课程"),

    /** 课程名额已满 */
    COURSE_FULL(4002, "课程已满，请选择其他课程"),

    /** 乐观锁冲突，请重试 */
    OPTIMISTIC_LOCK_FAILED(4003, "操作失败，请刷新后重试"),

    /** 抢课功能未开启 */
    ENROLLMENT_DISABLED(4004, "抢课功能暂未开启"),

    /** 超出抢课时间窗口 */
    ENROLLMENT_NOT_IN_WINDOW(4005, "当前非抢课时段，请等待开放"),

    /** 触发限流 */
    RATE_LIMIT_EXCEEDED(4006, "操作过于频繁，请稍后再试");

    /** 响应码 */
    private final int code;

    /** 响应消息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
