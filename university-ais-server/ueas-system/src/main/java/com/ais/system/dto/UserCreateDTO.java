package com.ais.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户创建请求 DTO。
 *
 * <p>封装新增用户时提交的信息，包含用户名、密码等必要字段。
 */
@Data
public class UserCreateDTO {
    /** 用户名（登录账号） */
    @NotBlank
    private String username;
    /** 初始密码 */
    @NotBlank
    private String password;
    /** 真实姓名 */
    @NotBlank
    private String realName;
    /** 用户类型：1=学生, 2=教师, 3=管理员 */
    @NotNull
    private Integer userType;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 状态：0=禁用, 1=正常（默认正常） */
    private Integer status = 1;
}
