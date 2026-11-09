package com.ais.system.mapper;

import com.ais.system.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色菜单关联数据访问对象。
 *
 * <p>提供角色与菜单关联表的 CRUD 操作。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色 ID 查询该角色关联的所有菜单。
     *
     * @param roleId 角色 ID
     * @return 角色菜单关联列表
     */
    List<SysRoleMenu> selectByRoleId(Long roleId);

    /**
     * 根据角色 ID 删除该角色的所有菜单关联。
     *
     * @param roleId 角色 ID
     */
    void deleteByRoleId(Long roleId);
}
