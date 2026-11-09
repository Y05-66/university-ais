package com.ais.system.mapper;

import com.ais.system.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单数据访问对象。
 *
 * <p>提供菜单表的 CRUD 操作，以及按角色 ID 查询菜单树的功能。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据角色 ID 查询该角色拥有的菜单列表（包含子菜单）。
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    List<SysMenu> selectTreeByRoleId(Long roleId);

    /**
     * 查询所有菜单（不分页）。
     *
     * @return 菜单列表
     */
    List<SysMenu> selectAllMenus();
}
