package com.ais.system.controller;

import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.security.util.SaTokenUtil;
import com.ais.system.entity.SysMenu;
import com.ais.system.mapper.SysMenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器。
 *
 * <p>提供菜单树查询、菜单增删改接口，支持按角色查询菜单权限。
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/sys-menu")
@RequiredArgsConstructor
public class SysMenuController {

    /** 菜单数据访问对象 */
    private final SysMenuMapper menuMapper;

    /**
     * 查询全部菜单树（平铺列表）。
     *
     * @return 菜单列表
     */
    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.ok(menuMapper.selectAllMenus());
    }

    /**
     * 根据角色 ID 查询该角色拥有的菜单树。
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    @Operation(summary = "根据角色ID查询菜单树")
    @GetMapping("/tree/{roleId}")
    public R<List<SysMenu>> treeByRole(@PathVariable Long roleId) {
        return R.ok(menuMapper.selectTreeByRoleId(roleId));
    }

    /**
     * 新增菜单。
     *
     * @param menu 菜单实体
     * @return 操作结果
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        menuMapper.insert(menu);
        return R.ok();
    }

    /**
     * 修改菜单信息。
     *
     * @param menu 菜单实体
     * @return 操作结果
     */
    @Operation(summary = "修改菜单")
    @PutMapping
    public R<Void> update(@RequestBody SysMenu menu) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        menuMapper.updateById(menu);
        return R.ok();
    }

    /**
     * 删除菜单（检查是否存在子菜单）。
     *
     * @param id 菜单 ID
     * @return 操作结果
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        List<SysMenu> children = menuMapper.selectAllMenus().stream()
                .filter(m -> m.getParentId() != null && m.getParentId().equals(id))
                .toList();
        if (!children.isEmpty()) {
            return R.fail("存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
        return R.ok();
    }
}
