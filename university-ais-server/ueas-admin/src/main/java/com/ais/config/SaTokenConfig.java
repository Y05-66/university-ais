package com.ais.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 安全配置。
 *
 * <p>注册全局登录拦截器，对未公开接口进行身份验证。
 * 通过 {@code excludePathPatterns} 列出免登录的公开接口路径。
 *
 * <p>公开接口分类：
 * <ul>
 *   <li>认证接口 — /auth/login（登录入口）</li>
 *   <li>基础数据查询 — 学院/专业/班级/课程/学生/教师的列表接口（下拉框用）</li>
 *   <li>成绩查询 — /score/my、/score/teacher（学生查看自己成绩、教师查看所教课程成绩）</li>
 *   <li>选课查询 — /enrollment/my、/enrollment/available（学生查看已选课程和可选课程）</li>
 *   <li>考试查询 — /exam/page、/exam/room、/exam/invigilate（公开考试信息）</li>
 *   <li>评价查询 — /evaluation/questionnaire/page（公开评价问卷）</li>
 *   <li>通知查询 — /notice/page、/notice/my（公开通知列表）</li>
 *   <li>系统管理 — 用户/角色/菜单/日志/字典的查询接口（管理员工具）</li>
 *   <li>文档资源 — Swagger/Knife4j 文档地址</li>
 * </ul>
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器。
     *
     * <p>拦截所有请求，对未排除的路径检查登录状态。
     * 未登录用户访问受保护接口时，Sa-Token 会抛出 NotLoginException，
     * 由全局异常处理器返回 401 响应。
     *
     * @param registry MVC 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 登录接口（无 Token 可访问）
                        "/auth/login",
                        "/api/auth/login",
                        "/auth/info",
                        "/api/auth/info",
                        "/auth/logout",
                        "/api/auth/logout",
                        // 基础数据查询（下拉框常用）
                        "/basic/semester/list", "/api/basic/semester/list",
                        "/basic/college/list", "/api/basic/college/list",
                        "/basic/major/page", "/api/basic/major/page",
                        "/basic/class/list", "/api/basic/class/list",
                        "/basic/class/page", "/api/basic/class/page",
                        // 课程相关（选课下拉框）
                        "/course/page", "/api/course/page",
                        "/course/list", "/api/course/list",
                        "/course-offer/page", "/api/course-offer/page",
                        "/course-offer/list", "/api/course-offer/list",
                        "/course-offer/timetable", "/api/course-offer/timetable",
                        "/course-offer/timetable-query", "/api/course-offer/timetable-query",
                        // 学生、教师列表（下拉框筛选）
                        "/student/page", "/api/student/page",
                        "/student/list", "/api/student/list",
                        "/teacher/page", "/api/teacher/page",
                        "/teacher/list", "/api/teacher/list",
                        // 成绩查询（学生查看自己成绩、教师查看所教成绩）
                        "/score/my", "/api/score/my",
                        "/score/teacher", "/api/score/teacher",
                        // 成绩录入（管理员/教师按学生视图，后端按角色过滤）
                        "/score/input", "/api/score/input",
                        "/score/input/list", "/api/score/input/list",
                        "/score/student/input/list", "/api/score/student/input/list",
                        // 选课查询
                        "/enrollment/my", "/api/enrollment/my",
                        "/enrollment/available", "/api/enrollment/available",
                        "/enrollment/roster/*", "/api/enrollment/roster/*",
                        // 考试查询（含个人视图）
                        "/exam/page", "/api/exam/page",
                        "/exam/my", "/api/exam/my",
                        "/exam/room", "/api/exam/room",
                        "/exam/room/page", "/api/exam/room/page",
                        "/exam/invigilate", "/api/exam/invigilate",
                        "/exam/invigilate/page", "/api/exam/invigilate/page",
                        "/exam/invigilate/my", "/api/exam/invigilate/my",
                        // 评价查询
                        "/evaluation/questionnaire/page", "/api/evaluation/questionnaire/page",
                        // 通知查询
                        "/notice/page", "/api/notice/page",
                        "/notice/my", "/api/notice/my",
                        "/notice/{id}", "/api/notice/{id}",
                        // 系统管理查询接口（管理员用）
                        "/sys-user/page", "/api/sys-user/page",
                        "/sys-role/list", "/api/sys-role/list",
                        "/sys-menu/tree", "/api/sys-menu/tree",
                        "/sys-log/page", "/api/sys-log/page",
                        "/sys-dict/list", "/api/sys-dict/list",
                        // 文档和静态资源
                        "/doc.html", "/webjars/**",
                        "/v3/api-docs/**", "/swagger-resources/**",
                        "/favicon.ico", "/error",
                        // 文件上传/下载接口（头像等）
                        "/upload/**", "/avatar/**",
                        "/api/upload/**", "/api/avatar/**",
                        // 验证码接口（免登录）
                        "/captcha", "/api/captcha",
                        "/auth/captcha", "/api/auth/captcha",
                        "/captcha/verify", "/api/captcha/verify",
                        "/auth/captcha/verify", "/api/auth/captcha/verify",
                        // AI 智能助手接口（免登录）
                        "/ai/chat", "/api/ai/chat",
                        "/ai/chat/student", "/api/ai/chat/student",
                        "/ai/chat/teacher", "/api/ai/chat/teacher",
                        "/ai/chat/admin", "/api/ai/chat/admin",
                        // Dashboard 相关接口（首页统计数据无需登录）
                        "/dashboard/stats", "/api/dashboard/stats",
                        // 抢课配置查询
                        "/sys-config/enrollment/enabled", "/api/sys-config/enrollment/enabled"
                );
    }
}
