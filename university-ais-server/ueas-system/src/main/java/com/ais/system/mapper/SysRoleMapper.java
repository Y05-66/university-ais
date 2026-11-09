package com.ais.system.mapper;

import com.ais.system.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据访问对象。
 *
 * <p>提供角色表的 CRUD 操作，以及查询角色及其关联权限的自定义方法。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询所有角色及其关联的权限标识（逗号分隔）。
     *
     * @return 角色列表（含 permissions 字段）
     */
    @Select("SELECT r.*, GROUP_CONCAT(m.permission) as permissions " +
            "FROM sys_role r " +
            "LEFT JOIN sys_role_menu rm ON r.id = rm.role_id " +
            "LEFT JOIN sys_menu m ON rm.menu_id = m.id " +
            "GROUP BY r.id ORDER BY r.sort")
    List<SysRole> selectRolesWithPermissions();
}
