package com.ais.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.security.util.SaTokenUtil;
import com.ais.system.dto.UserCreateDTO;
import com.ais.system.dto.UserQueryDTO;
import com.ais.system.entity.SysUser;
import com.ais.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器。
 *
 * <p>提供用户的增删改查、密码重置、列表查询等接口。
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/sys-user")
@RequiredArgsConstructor
@Slf4j
public class SysUserController {

    /** 用户服务 */
    private final SysUserService userService;

    /**
     * 分页查询用户列表。
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public R<PageResult<SysUser>> page(UserQueryDTO query) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        return R.ok(userService.pageQuery(query));
    }

    /**
     * 根据 ID 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @Operation(summary = "根据ID查询用户详情")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        SysUser user = userService.getById(id);
        if (user == null) {
            return R.fail(ResultCode.NOT_FOUND);
        }
        return R.ok(user);
    }

    /**
     * 新增用户。
     *
     * @param dto 用户创建请求体
     * @return 操作结果
     */
    @Operation(summary = "新增用户")
    @PostMapping
    public R<Void> add(@Valid @RequestBody UserCreateDTO dto) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        log.info("创建用户: username={}, userType={}", dto.getUsername(), dto.getUserType());
        userService.create(dto);
        return R.ok();
    }

    /**
     * 修改用户信息。
     *
     * @param user 用户实体
     * @return 操作结果
     */
    @Operation(summary = "修改用户信息")
    @PutMapping
    public R<Void> update(@RequestBody SysUser user) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        log.info("更新用户: id={}", user.getId());
        userService.update(user);
        return R.ok();
    }

    /**
     * 删除用户（逻辑删除）。
     *
     * @param id 用户 ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        log.info("删除用户: id={}", id); userService.deleteById(id);
        return R.ok();
    }

    /**
     * 重置用户密码。
     *
     * @param id      用户 ID
     * @param body    包含新密码的请求体
     * @return 操作结果
     */
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        if (body == null || body.get("newPassword") == null || body.get("newPassword").isEmpty()) {
            return R.fail("新密码不能为空");
        }
        userService.resetPassword(id, body.get("newPassword"));
        return R.ok();
    }

    /**
     * 获取所有可用用户列表（用于下拉选择）。
     *
     * @return 用户列表
     */
    @Operation(summary = "获取所有可用用户")
    @GetMapping("/list")
    public R<List<SysUser>> list() {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        return R.ok(userService.listAllActive());
    }
}
