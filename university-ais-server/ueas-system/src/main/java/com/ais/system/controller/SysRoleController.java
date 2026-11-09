package com.ais.system.controller;

import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.security.util.SaTokenUtil;
import com.ais.system.entity.SysRole;
import com.ais.system.entity.SysRoleMenu;
import com.ais.system.mapper.SysRoleMapper;
import com.ais.system.mapper.SysRoleMenuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色管理控制器。
 *
 * <p>提供角色的增删改查以及角色菜单权限管理接口。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/sys-role")
@RequiredArgsConstructor
public class SysRoleController {

    /** 角色数据访问对象 */
    private final SysRoleMapper roleMapper;
    /** 角色菜单关联数据访问对象 */
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 查询所有角色列表（含权限标识）。
     *
     * @return 角色列表
     */
    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public R<List<SysRole>> list() {
        return R.ok(roleMapper.selectRolesWithPermissions());
    }

    /**
     * 根据 ID 查询角色详情。
     *
     * @param id 角色 ID
     * @return 角色详情
     */
    @Operation(summary = "根据ID查询角色")
    @GetMapping("/{id}")
    public R<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            return R.fail(ResultCode.NOT_FOUND);
        }
        return R.ok(role);
    }

    /**
     * 新增角色。
     *
     * @param role 角色实体
     * @return 操作结果
     */
    @Operation(summary = "新增角色")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        roleMapper.insert(role);
        return R.ok();
    }

    /**
     * 修改角色信息。
     *
     * @param role 角色实体
     * @return 操作结果
     */
    @Operation(summary = "修改角色")
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        roleMapper.updateById(role);
        return R.ok();
    }

    /**
     * 删除角色（同时删除角色菜单关联）。
     *
     * @param id 角色 ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        // 不允许删除管理员角色
        SysRole role = roleMapper.selectById(id);
        if (role != null && "ROLE_ADMIN".equals(role.getCode())) {
            return R.fail("不能删除管理员角色");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.deleteByRoleId(id);
        return R.ok();
    }

    /**
     * 获取角色已分配的菜单 ID 列表。
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表
     */
    @Operation(summary = "获取角色的菜单权限")
    @GetMapping("/{roleId}/menus")
    public R<List<Long>> getMenuIds(@PathVariable Long roleId) {
        List<SysRoleMenu> menus = roleMenuMapper.selectByRoleId(roleId);
        return R.ok(menus.stream().map(SysRoleMenu::getMenuId).toList());
    }

    /**
     * 为角色分配菜单权限。
     *
     * @param roleId   角色 ID
     * @param menuIds  要分配的菜单 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "设置角色菜单权限")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/{roleId}/menus")
    public R<Void> setMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
        return R.ok();
    }
}
