package com.ais.system.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.ais.common.exception.BizException;
import com.ais.common.result.PageResult;
import com.ais.common.result.ResultCode;
import com.ais.system.dto.LoginResult;
import com.ais.system.dto.UserInfo;
import com.ais.system.dto.LoginDTO;
import com.ais.system.dto.UserCreateDTO;
import com.ais.system.dto.UserQueryDTO;
import com.ais.system.dto.UserUpdateDTO;
import com.ais.system.entity.*;
import com.ais.system.mapper.*;
import com.ais.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类。
 *
 * <p>处理用户认证、注册、查询、修改等核心业务逻辑。
 * 使用 Sa-Token 进行会话管理，密码采用 BCrypt 哈希存储。
 *
 * <p>主要功能：
 * <ul>
 *   <li>用户登录 - 验证用户名密码，创建 Sa-Token 会话</li>
 *   <li>获取用户信息 - 返回用户详情和权限菜单</li>
 *   <li>用户CRUD - 创建、查询、更新、删除用户</li>
 *   <li>密码管理 - 重置密码、修改密码</li>
 *   <li>权限构建 - 根据角色生成菜单树</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "ueas:captcha:";
    private static final String LOGIN_FAIL_KEY_PREFIX = "ueas:login:fail:";
    private static final int MAX_LOGIN_FAILS = 5;
    private static final long LOCKOUT_MINUTES = 15;

    @Override
    public LoginResult login(LoginDTO dto) {
        // 校验验证码
        String captchaValue = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + dto.getCaptchaKey());
        if (captchaValue == null) {
            throw new BizException(ResultCode.CAPTCHA_EXPIRED);
        }
        if (!captchaValue.equalsIgnoreCase(dto.getCaptcha())) {
            redisTemplate.delete(CAPTCHA_KEY_PREFIX + dto.getCaptchaKey());
            log.warn("验证码错误: username={}", dto.getUsername());
            throw new BizException(ResultCode.CAPTCHA_ERROR);
        }
        redisTemplate.delete(CAPTCHA_KEY_PREFIX + dto.getCaptchaKey());

        // 检查是否因多次登录失败被锁定
        String failKey = LOGIN_FAIL_KEY_PREFIX + dto.getUsername();
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        if (failCountStr != null) {
            try {
                if (Integer.parseInt(failCountStr) >= MAX_LOGIN_FAILS) {
                    log.warn("登录失败: 账号已锁定, username={}", dto.getUsername());
                    throw new BizException("账号因多次登录失败已锁定" + LOCKOUT_MINUTES + "分钟");
                }
            } catch (NumberFormatException nfe) {
                log.warn("登录失败: Redis中失败计数格式异常, username={}", dto.getUsername());
                redisTemplate.delete(failKey);
            }
        }

        // 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .eq(SysUser::getDeleted, 0)
        );
        if (user == null || user.getStatus() == 0) {
            log.warn("登录失败: 用户不存在或已禁用, username={}", dto.getUsername());
            throw new BizException(ResultCode.LOGIN_FAILED);
        }

        // 验证密码（BCrypt 兼容 SHA-256 旧数据）
        if (!checkPassword(dto.getPassword(), user.getPassword())) {
            // 记录失败次数
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            if (failCount == 1) {
                redisTemplate.expire(failKey, LOCKOUT_MINUTES, TimeUnit.MINUTES);
            }
            if (failCount >= MAX_LOGIN_FAILS) {
                log.warn("登录失败: 账号已锁定, username={}", dto.getUsername());
                throw new BizException("账号因多次登录失败已锁定" + LOCKOUT_MINUTES + "分钟");
            }
            log.warn("登录失败: 密码错误, username={}, failCount={}", dto.getUsername(), failCount);
            throw new BizException(ResultCode.LOGIN_FAILED);
        }

        // 登录成功，清除失败计数
        redisTemplate.delete(failKey);

        // Sa-Token 登录
        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("realName", user.getRealName());
        StpUtil.getSession().set("userType", user.getUserType());

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        // 批量查询角色（避免 N+1）
        List<SysUserRole> roles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        List<String> roleCodes = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>();
        if (!roles.isEmpty()) {
            roleIds = roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roleList = roleMapper.selectBatchIds(roleIds);
            roleCodes = roleList.stream().map(SysRole::getCode).collect(Collectors.toList());
        }

        // 构建菜单树
        List<SysMenu> allMenus = mergeMenus(roleIds);
        List<Map<String, Object>> menuList = buildMenuTree(allMenus, user.getUserType());

        // 组装响应
        LoginResult result = new LoginResult();
        result.setToken(StpUtil.getTokenValue());
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setUserType(user.getUserType());
        result.setRoles(roleCodes);
        result.setMenus(menuList);

        log.info("用户登录成功: username={}, userType={}", user.getUsername(), user.getUserType());
        return result;
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        SysUser user = userMapper.selectDetailById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        List<SysUserRole> roles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        List<String> roleCodes = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>();
        if (!roles.isEmpty()) {
            roleIds = roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roleList = roleMapper.selectBatchIds(roleIds);
            roleCodes = roleList.stream().map(SysRole::getCode).collect(Collectors.toList());
        }

        List<SysMenu> allMenus = mergeMenus(roleIds);
        List<Map<String, Object>> menuList = buildMenuTree(allMenus, user.getUserType());

        UserInfo info = new UserInfo();
        info.setUserId(user.getId());
        info.setUsername(user.getUsername());
        info.setRealName(user.getRealName());
        info.setUserType(user.getUserType());
        info.setPhone(user.getPhone());
        info.setEmail(user.getEmail());
        info.setAvatar(user.getAvatar());
        info.setRoles(roleCodes);
        info.setMenus(menuList);
        return info;
    }

    @Override
    public PageResult<SysUser> pageQuery(UserQueryDTO query) {
        IPage<SysUser> page = userMapper.pageQuery(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(query.getCurrent(), query.getSize()),
                query
        );
        return PageResult.of(page);
    }

    @Override
    public SysUser getById(Long id) {
        SysUser user = userMapper.selectDetailById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserCreateDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .eq(SysUser::getDeleted, 0)
        );
        if (count > 0) {
            throw new BizException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        // 密码强度校验
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new BizException("密码长度不能少于8位");
        }
        if (!dto.getPassword().matches(".*[a-zA-Z].*") || !dto.getPassword().matches(".*[0-9].*")) {
            throw new BizException("密码必须包含字母和数字");
        }
        user.setPassword(hashPassword(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setUserType(dto.getUserType());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
        userMapper.insert(user);

        // 分配默认角色
        SysRole defaultRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getCode, getRoleCodeByType(dto.getUserType()))
        );
        if (defaultRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(defaultRole.getId());
            userRoleMapper.insert(ur);
        }

        log.info("创建用户成功: username={}, userType={}", dto.getUsername(), dto.getUserType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user) {
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        userMapper.updateById(user);
        log.info("更新用户: id={}", user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException("无效的用户ID");
        }
        // 不能删除自己
        if (StpUtil.isLogin() && id.equals(StpUtil.getLoginIdAsLong())) {
            throw new BizException("不能删除当前登录账号");
        }
        SysUser user = userMapper.selectById(id);
        userMapper.deleteById(id);
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id)
        );
        log.info("删除用户: id={}, username={}", id, user != null ? user.getUsername() : "unknown");
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        // 密码强度校验：至少8位，包含字母和数字
        if (newPassword == null || newPassword.length() < 8) {
            throw new BizException("密码长度不能少于8位");
        }
        if (!newPassword.matches(".*[a-zA-Z].*") || !newPassword.matches(".*[0-9].*")) {
            throw new BizException("密码必须包含字母和数字");
        }
        user.setPassword(hashPassword(newPassword));
        userMapper.updateById(user);
        log.info("重置密码: id={}", id);
    }

    @Override
    public List<SysUser> listAllActive() {
        return userMapper.selectAllActive();
    }

    @Override
    public void updateProfile(Long userId, UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        log.info("更新个人资料: userId={}", userId);
    }

    /**
     * BCrypt 密码验证。
     */
    public boolean checkPassword(String rawPassword, String storedHash) {
        return BCrypt.checkpw(rawPassword, storedHash);
    }

    /**
     * 使用 BCrypt 加密密码。
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password);
    }

    private String getRoleCodeByType(Integer userType) {
        return switch (userType) {
            case 1 -> "ROLE_STUDENT";
            case 2 -> "ROLE_TEACHER";
            case 3 -> "ROLE_ADMIN";
            default -> "ROLE_STUDENT";
        };
    }

    private List<SysMenu> mergeMenus(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, SysMenu> menuMap = new LinkedHashMap<>();
        for (Long roleId : roleIds) {
            List<SysMenu> menus = menuMapper.selectTreeByRoleId(roleId);
            if (menus != null) {
                for (SysMenu menu : menus) {
                    menuMap.putIfAbsent(menu.getId(), menu);
                }
            }
        }
        return new ArrayList<>(menuMap.values());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menus, Integer userType) {
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();

        for (SysMenu menu : menus) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", menu.getId());
            node.put("parentId", menu.getParentId());
            node.put("name", menu.getName());
            node.put("path", menu.getPath());
            node.put("component", menu.getComponent());
            node.put("icon", menu.getIcon());
            node.put("type", menu.getType());
            node.put("permission", menu.getPermission());
            node.put("sort", menu.getSort());
            node.put("visible", menu.getVisible());
            nodeMap.put(menu.getId(), node);
        }

        List<SysMenu> sortedMenus = menus.stream()
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .collect(Collectors.toList());

        for (SysMenu menu : sortedMenus) {
            Map<String, Object> node = nodeMap.get(menu.getId());
            if (node == null) continue;
            Long parentId = (Long) node.get("parentId");
            String path = (String) node.get("path");

            if (userType != null && userType == 3
                    && path != null && (path.equals("/enrollment/my") || path.equals("/score/my"))) {
                continue;
            }

            if (parentId == 0 || parentId == null) {
                result.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(parentId);
                if (parent != null) {
                    if (!parent.containsKey("children")) {
                        parent.put("children", new ArrayList<>());
                    }
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                }
            }
        }
        return result;
    }
}
