package com.ais.system.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 用户信息更新请求 DTO。
 *
 * <p>封装修改用户个人信息时提交的数据，不包含密码修改。
 */
@Data
public class UserUpdateDTO {
    /** 用户 ID */
    private Long id;
    /** 真实姓名 */
    private String realName;
    /** 手机号（最大 20 字符） */
    @Size(max = 20)
    private String phone;
    /** 邮箱（最大 100 字符，需符合邮箱格式） */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100)
    private String email;
    /** 头像 URL */
    private String avatar;
}
