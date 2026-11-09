package com.ais.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联实体。
 *
 * <p>对应数据库表 {@code sys_user_role}，实现用户与角色的多对多关系。
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {
    /** 主键 ID */
    private Long id;
    /** 用户 ID（关联 sys_user 表） */
    private Long userId;
    /** 角色 ID（关联 sys_role 表） */
    private Long roleId;
}
