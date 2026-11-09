package com.ais.system.config;

import cn.dev33.satoken.stp.StpInterface;
import com.ais.system.entity.SysRole;
import com.ais.system.entity.SysUserRole;
import com.ais.system.mapper.SysRoleMapper;
import com.ais.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限接口实现。
 *
 * <p>Sa-Token 通过此接口动态获取用户的权限信息，用于权限校验。
 * 实现 {@link StpInterface} 接口后，Sa-Token 会在每次权限检查时调用此方法。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 获取用户权限列表。
     *
     * <p>根据当前登录用户 ID 查询其角色，再查询角色对应的权限标识。
     *
     * @param loginId 登录用户 ID
     * @return 权限标识列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 查询用户关联的角色 ID
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, loginId));
        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 查询角色及其权限
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);

        // 从每个角色的 permissions 字段提取权限标识
        List<String> permissions = new ArrayList<>();
        for (SysRole role : roles) {
            if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
                permissions.addAll(Arrays.asList(role.getPermissions().split(",")));
            }
        }
        return permissions;
    }

    /**
     * 获取用户角色列表。
     *
     * <p>根据用户 ID 查询其关联的角色编码列表。
     *
     * @param loginId 登录用户 ID
     * @return 角色编码列表（如 ["ROLE_ADMIN", "ROLE_TEACHER"]）
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 查询用户关联的角色 ID
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, loginId));
        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 查询角色编码
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());
    }
}
