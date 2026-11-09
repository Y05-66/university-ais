package com.ais.system.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 菜单实体。
 *
 * <p>对应数据库表 {@code sys_menu}，存储系统菜单和按钮的定义信息。
 *
 * <p>菜单类型（type）：
 * <ul>
 *   <li>0 — 目录（父级菜单，无页面）</li>
 *   <li>1 — 菜单（可点击的页面入口）</li>
 *   <li>2 — 按钮（页面内的操作按钮，如新增、删除）</li>
 * </ul>
 *
 * <p>可见性（visible）：
 * <ul>
 *   <li>0 — 隐藏（不显示在侧边栏）</li>
 *   <li>1 — 显示</li>
 * </ul>
 */
@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父菜单 ID，0 表示根菜单 */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 路由路径（如 /dashboard） */
    private String path;

    /** 前端组件路径（如 dashboard/index） */
    private String component;

    /** 菜单图标（Element Plus 图标名） */
    private String icon;

    /** 菜单类型：0=目录, 1=菜单, 2=按钮 */
    private Integer type;

    /** 权限标识（如 system:user:query） */
    private String permission;

    /** 排序号，数字越小越靠前 */
    private Integer sort;

    /** 可见性：0=隐藏, 1=显示 */
    private Integer visible;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
