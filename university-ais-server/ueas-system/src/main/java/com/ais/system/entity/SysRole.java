package com.ais.system.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色实体。
 *
 * <p>对应数据库表 {@code sys_role}，存储系统角色的定义信息。
 *
 * <p>预置角色：
 * <ul>
 *   <li>ROLE_ADMIN — 系统管理员（拥有所有权限）</li>
 *   <li>ROLE_ACADEMIC — 教务管理员（基础管理、教务管理）</li>
 *   <li>ROLE_TEACHER — 教师（成绩录入、我的选课）</li>
 *   <li>ROLE_STUDENT — 学生（我的成绩、我的选课）</li>
 * </ul>
 */
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称（如"系统管理员"） */
    private String name;

    /** 角色编码（用于权限判断，如 ROLE_ADMIN） */
    private String code;

    /** 权限标识列表（逗号分隔，由 @Select 查询动态填充，不映射到数据库列） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String permissions;

    /** 排序号，数字越小越靠前 */
    private Integer sort;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
