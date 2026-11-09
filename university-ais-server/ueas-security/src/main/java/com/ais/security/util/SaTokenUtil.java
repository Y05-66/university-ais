package com.ais.security.util;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 工具类。
 *
 * <p>封装 Sa-Token 的常用操作，提供静态方法以便在任意位置获取当前登录用户信息。
 *
 * <p>主要功能：
 * <ul>
 *   <li>获取当前登录用户 ID</li>
 *   <li>获取当前登录用户名</li>
 *   <li>获取当前用户类型（1=学生, 2=教师, 3=管理员）</li>
 *   <li>角色和权限校验</li>
 * </ul>
 */
@Component
public class SaTokenUtil {

    /**
     * 获取当前登录用户的 ID。
     *
     * @return 用户 ID，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户的用户名。
     *
     * @return 用户名，未登录时返回 null
     */
    public static String getCurrentUsername() {
        try {
            Object user = StpUtil.getSession().get("username");
            return user != null ? user.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户的类型。
     *
     * @return 用户类型（1=学生, 2=教师, 3=管理员），未登录时返回 null
     */
    public static Integer getCurrentUserType() {
        try {
            Object type = StpUtil.getSession().get("userType");
            return type != null ? Integer.parseInt(type.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查当前用户是否拥有指定角色。
     *
     * @param role 角色编码（如 ROLE_ADMIN）
     * @return true=拥有该角色, false=无该角色
     */
    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }

    /**
     * 检查当前用户是否拥有指定权限。
     *
     * @param permission 权限标识
     * @return true=拥有该权限, false=无该权限
     */
    public static boolean hasPermission(String permission) {
        return StpUtil.hasPermission(permission);
    }

    /**
     * 检查当前用户是否拥有指定角色，否则抛出异常。
     *
     * @param role 角色编码
     * @throws cn.dev33.satoken.exception.NotPermissionException 如果用户无该角色
     */
    public static void checkRole(String role) {
        StpUtil.checkRole(role);
    }

    /**
     * 检查当前用户是否拥有指定权限，否则抛出异常。
     *
     * @param permission 权限标识
     * @throws cn.dev33.satoken.exception.NotPermissionException 如果用户无该权限
     */
    public static void checkPermission(String permission) {
        StpUtil.checkPermission(permission);
    }
}
