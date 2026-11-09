package com.ais.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联实体。
 *
 * <p>对应数据库表 {@code sys_role_menu}，实现角色与菜单的多对多关系。
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {
    /** 主键 ID */
    private Long id;
    /** 角色 ID（关联 sys_role 表） */
    private Long roleId;
    /** 菜单 ID（关联 sys_menu 表） */
    private Long menuId;
}
