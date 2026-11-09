package com.ais.system.dto;

import lombok.Data;

/**
 * 用户信息 DTO。
 *
 * <p>替代 Map&lt;String, Object&gt;，提供类型安全的用户信息响应。
 */
@Data
public class UserInfo {
    /** 用户 ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 真实姓名 */
    private String realName;
    /** 用户类型：1=学生, 2=教师, 3=管理员 */
    private Integer userType;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 头像 URL */
    private String avatar;
    /** 角色编码列表 */
    private java.util.List<String> roles;
    /** 菜单树（服务端生成） */
    private java.util.List<java.util.Map<String, Object>> menus;
}
