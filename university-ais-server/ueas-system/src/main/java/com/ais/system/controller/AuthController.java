package com.ais.system.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.system.dto.LoginDTO;
import com.ais.system.dto.LoginResult;
import com.ais.system.dto.UserInfo;
import com.ais.system.dto.UserCreateDTO;
import com.ais.system.dto.UserQueryDTO;
import com.ais.system.dto.UserUpdateDTO;
import com.ais.system.entity.SysUser;
import com.ais.system.service.SysUserService;
import com.ais.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器。
 *
 * <p>处理用户登录、登出、获取用户信息、修改个人资料和密码等操作。
 * 所有接口路径以 /auth 开头，登录相关接口标记 @SaIgnore 免登录访问。
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SysUserService userService;

    @Operation(summary = "用户登录")
    @SaIgnore
    @PostMapping("/login")
    public R<LoginResult> login(@Valid @RequestBody LoginDTO dto) {
        log.debug("登录尝试: username={}", dto.getUsername());
        return R.ok(userService.login(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @SaIgnore
    @GetMapping("/info")
    public R<UserInfo> getUserInfo() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return R.ok(userService.getUserInfo(userId));
        } catch (cn.dev33.satoken.exception.NotLoginException e) {
            return R.fail(ResultCode.UNAUTHORIZED);
        }
    }

    @Operation(summary = "退出登录")
    @SaIgnore
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public R<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, dto);
        return R.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> updatePassword(@RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null
                || oldPassword.isEmpty() || newPassword.isEmpty()) {
            return R.fail(ResultCode.PARAM_INVALID);
        }
        SysUser user = userService.getById(userId);
        if (user == null) {
            return R.fail(ResultCode.USER_NOT_FOUND);
        }
        // BCrypt 验证旧密码（兼容 SHA-256 迁移期）
        if (!userService.checkPassword(oldPassword, user.getPassword())) {
            return R.fail("原密码错误");
        }
        userService.resetPassword(userId, newPassword);
        // 密码修改后强制重新登录，使旧 token 失效
        StpUtil.logout();
        log.info("用户修改密码并强制重新登录: userId={}", userId);
        return R.ok();
    }
}
