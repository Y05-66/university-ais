package com.ais.system.service;

import com.ais.common.result.PageResult;
import com.ais.system.dto.LoginDTO;
import com.ais.system.dto.LoginResult;
import com.ais.system.dto.UserInfo;
import com.ais.system.dto.UserCreateDTO;
import com.ais.system.dto.UserQueryDTO;
import com.ais.system.dto.UserUpdateDTO;
import com.ais.system.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口。
 *
 * <p>提供用户认证、用户信息管理、密码重置等功能。
 */
public interface SysUserService {
    /**
     * 用户登录。
     *
     * @param dto 登录凭证
     * @return 登录结果（token、用户信息、角色、菜单）
     */
    LoginResult login(LoginDTO dto);
    /**
     * 获取当前用户详细信息。
     *
     * @param userId 用户 ID
     * @return 用户信息（含角色、菜单）
     */
    UserInfo getUserInfo(Long userId);
    /**
     * 分页查询用户列表。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<SysUser> pageQuery(UserQueryDTO query);
    /**
     * 根据 ID 获取用户详情。
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    SysUser getById(Long id);
    /**
     * 新增用户。
     *
     * @param dto 用户创建参数
     */
    void create(UserCreateDTO dto);
    /**
     * 更新用户信息。
     *
     * @param user 用户实体
     */
    void update(SysUser user);
    /**
     * 删除用户（逻辑删除）。
     *
     * @param id 用户 ID
     */
    void deleteById(Long id);
    /**
     * 重置用户密码。
     *
     * @param id           用户 ID
     * @param newPassword  新密码（明文）
     */
    void resetPassword(Long id, String newPassword);
    /**
     * 查询所有正常状态的用户列表。
     *
     * @return 用户列表
     */
    List<SysUser> listAllActive();
    /**
     * 更新用户个人信息。
     *
     * @param userId 用户 ID
     * @param dto    更新参数
     */
    void updateProfile(Long userId, UserUpdateDTO dto);
    /**
     * BCrypt 密码校验，兼容旧版 SHA-256 数据。
     *
     * @param rawPassword 明文密码
     * @param storedHash  已存储的哈希值
     * @return 是否匹配
     */
    boolean checkPassword(String rawPassword, String storedHash);
}
