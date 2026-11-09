package com.ais.system.mapper;

import com.ais.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户角色关联数据访问对象。
 *
 * <p>提供用户与角色关联表的 CRUD 操作。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户 ID 查询该用户关联的所有角色。
     *
     * @param userId 用户 ID
     * @return 用户角色关联列表
     */
    List<SysUserRole> selectByUserId(Long userId);

    /**
     * 根据角色 ID 查询该角色的所有用户。
     *
     * @param roleId 角色 ID
     * @return 用户角色关联列表
     */
    List<SysUserRole> selectByRoleId(Long roleId);
}
