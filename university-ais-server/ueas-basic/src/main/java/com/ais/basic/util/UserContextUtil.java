package com.ais.basic.util;

import cn.dev33.satoken.stp.StpUtil;
import com.ais.basic.entity.Student;
import com.ais.basic.mapper.StudentMapper;
import com.ais.basic.mapper.TeacherMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户上下文工具类。
 *
 * <p>通过 Sa-Token session 或请求属性获取当前登录用户信息，
 * 将当前登录用户（sys_user）转换为对应的学生/教师实体 ID，
 * 方便业务层直接使用 student.id 或 teacher.id 进行查询。
 */
@Component
public class UserContextUtil {

    private static final String ATTR_USER_ID = "currentUserId";
    private static final String ATTR_USER_TYPE = "currentUserType";

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    public UserContextUtil(StudentMapper studentMapper, TeacherMapper teacherMapper) {
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
    }

    /**
     * 获取当前登录用户的学生 ID。
     *
     * @return 学生 ID，未登录或非学生用户时返回 null
     */
    public Long getStudentId() {
        Integer userType = getUserType();
        if (userType == null || userType != 1) {
            return null;
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        Student student = studentMapper.selectByUserId(userId);
        return student != null ? student.getId() : null;
    }

    /**
     * 获取当前登录用户的教师 ID 列表。
     *
     * <p>管理员（userType=3）查询全部教师 ID，普通教师返回关联的教师 ID。
     *
     * @return 教师 ID 列表，非教师/管理用户返回 null
     */
    public List<Long> getTeacherIds() {
        Integer userType = getUserType();
        if (userType == null || (userType != 2 && userType != 3)) {
            return null;
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        // 管理员查询全部教师 ID，普通教师只返回自己的记录
        if (userType == 3) {
            return teacherMapper.selectAllTeacherIds();
        }
        return teacherMapper.selectTeacherIdsByUserId(userId);
    }

    /**
     * 获取当前登录用户的类型。
     *
     * @return 用户类型：1=学生 2=教师 3=管理员，未登录时返回 null
     */
    public Integer getUserType() {
        // 优先从请求属性读取
        Object type = getAttribute(ATTR_USER_TYPE);
        if (type instanceof Number) {
            return ((Number) type).intValue();
        }
        if (type instanceof String) {
            try {
                return Integer.parseInt((String) type);
            } catch (NumberFormatException ignored) {
            }
        }
        // 回退到 Sa-Token session
        try {
            Object sessionType = StpUtil.getSession().get("userType");
            if (sessionType != null) {
                return Integer.parseInt(sessionType.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 获取当前登录用户的系统用户 ID。
     *
     * @return 系统用户 ID，未登录时返回 null
     */
    public Long getCurrentUserId() {
        // 优先从请求属性读取
        Object val = getAttribute(ATTR_USER_ID);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // 回退到 Sa-Token session
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Object getAttribute(String name) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest req = attrs.getRequest();
        return req.getAttribute(name);
    }
}
