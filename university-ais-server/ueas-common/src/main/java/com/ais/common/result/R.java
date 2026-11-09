package com.ais.common.result;

import lombok.Data;

/**
 * 统一 API 响应封装。
 *
 * <p>所有接口返回数据均使用此结构，前端通过 {@code res.code} 判断是否成功。
 *
 * <p>响应码规范：
 * <ul>
 *   <li>200 — 成功</li>
 *   <li>401 — 未登录或 Token 无效</li>
 *   <li>403 — 无权限</li>
 *   <li>500 — 系统内部错误</li>
 * </ul>
 *
 * @param <T> 响应数据体类型
 */
@Data
public class R<T> {

    /** 响应码：200=成功，其他为错误码 */
    private int code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /** 成功响应，无数据 */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        return r;
    }

    /** 成功响应，带数据 */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    /** 失败响应 */
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    /** 失败响应，自定义码和消息 */
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    /** 失败响应，使用预定义结果码 */
    public static <T> R<T> fail(ResultCode code) {
        R<T> r = new R<>();
        r.setCode(code.getCode());
        r.setMsg(code.getMessage());
        return r;
    }
}
