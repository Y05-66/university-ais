package com.ais.system.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 系统用户实体。
 *
 * <p>对应数据库表 {@code sys_user}，存储系统用户的基本信息和认证信息。
 *
 * <p>用户类型（userType）：
 * <ul>
 *   <li>1 — 学生</li>
 *   <li>2 — 教师</li>
 *   <li>3 — 管理员</li>
 * </ul>
 */
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 用户名（登录账号） */
    private String username;

    /**
     * 密码（SHA-256 加密）。
     * 序列化时忽略，避免在 API 响应中泄露密码。
     */
    @JsonIgnore
    private String password;

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

    /** 状态：0=禁用, 1=正常 */
    private Integer status;

    /** 最后登录时间 */
    private java.time.LocalDateTime lastLogin;
}
