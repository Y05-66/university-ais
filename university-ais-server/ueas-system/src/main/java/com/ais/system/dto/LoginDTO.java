package com.ais.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 DTO。
 *
 * <p>封装登录接口所需的用户凭证信息，包含用户名、密码和图形验证码。
 */
@Data
public class LoginDTO {
    /** 用户名（登录账号） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（BCrypt 哈希） */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码唯一标识，由 /captcha 接口返回 */
    @NotBlank(message = "验证码不能为空")
    private String captchaKey;

    /** 用户输入的验证码 */
    @NotBlank(message = "验证码不能为空")
    private String captcha;
}
