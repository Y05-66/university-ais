package com.ais.enrollment.controller;

import com.ais.basic.entity.CourseOffer;
import com.ais.basic.mapper.CourseOfferMapper;
import com.ais.common.config.RushConfig;
import com.ais.common.config.RushLimiter;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.enrollment.service.EnrollmentService;
import com.ais.security.util.SaTokenUtil;
import com.ais.basic.util.UserContextUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选课管理控制器。
 *
 * <p>提供学生选课、退课、查看选课列表和可选题库的功能接口。
 * 抢课场景下额外启用限流和时间窗口控制。
 */
@Tag(name = "选课管理")
@RestController
@RequestMapping("/enrollment")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseOfferMapper courseOfferMapper;
    private final UserContextUtil userContextUtil;
    private final StringRedisTemplate redisTemplate;
    private final RushConfig rushConfig;
    private final RushLimiter rushLimiter;

    @Operation(summary = "我的选课列表")
    @GetMapping("/my")
    public R<PageResult<Map<String, Object>>> my(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long semesterId) {
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return R.ok(PageResult.of(new Page<>(current, size)));
        }
        var iPage = enrollmentService.getMyCourses(studentId, semesterId, current, size);
        return R.ok(PageResult.of(iPage));
    }

    @Operation(summary = "选课（抢课）")
    @PostMapping("/select/{courseOfferId}")
    public R<Void> select(@PathVariable Long courseOfferId) {
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return R.fail("未登录");
        }

        // 1. 检查抢课总开关
        String enrollmentEnabled = redisTemplate.opsForValue().get("sys:config:enrollment_enabled");
        if (!"true".equals(enrollmentEnabled)) {
            return R.fail(ResultCode.ENROLLMENT_DISABLED);
        }

        // 2. 检查抢课时段（管理员可通过此控制抢课开始/结束时间）
        if (!rushConfig.isInRushWindow(redisTemplate)) {
            return R.fail(ResultCode.ENROLLMENT_NOT_IN_WINDOW.getCode(), "当前非抢课时段，请等待开放");
        }

        // 3. 限流：单用户每秒最多 N 次请求
        if (!rushLimiter.tryAcquire(studentId)) {
            return R.fail(ResultCode.RATE_LIMIT_EXCEEDED.getCode(), "操作过于频繁，请稍后再试");
        }

        enrollmentService.selectCourse(studentId, courseOfferId);
        return R.ok();
    }

    @Operation(summary = "退课")
    @PostMapping("/drop/{courseOfferId}")
    public R<Void> drop(@PathVariable Long courseOfferId) {
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return R.fail("未登录");
        }
        enrollmentService.dropCourse(studentId, courseOfferId);
        return R.ok();
    }

    @Operation(summary = "可选题库")
    @GetMapping("/available")
    public R<List<Map<String, Object>>> available(@RequestParam Long semesterId) {
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return R.ok(java.util.Collections.emptyList());
        }
        return R.ok(enrollmentService.getAvailableCourses(studentId, semesterId));
    }

    @Operation(summary = "选课名单")
    @GetMapping("/roster/{courseOfferId}")
    public R<List<Map<String, Object>>> roster(@PathVariable Long courseOfferId) {
        if (SaTokenUtil.hasRole("ROLE_ADMIN") || SaTokenUtil.hasRole("ROLE_ACADEMIC")) {
            return R.ok(enrollmentService.getEnrollmentRoster(courseOfferId));
        }
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        if (currentUserId == null) {
            return R.fail("未登录");
        }
        CourseOffer offer = courseOfferMapper.selectById(courseOfferId);
        if (offer == null) {
            return R.fail("课程不存在");
        }
        List<Long> teacherIds = userContextUtil.getTeacherIds();
        boolean isCourseInstructor = teacherIds != null
                && offer.getTeacherId() != null
                && teacherIds.contains(offer.getTeacherId());
        if (isCourseInstructor) {
            return R.ok(enrollmentService.getEnrollmentRoster(courseOfferId));
        }
        return R.fail("无权查看该选课名单");
    }

    // ==================== 抢课时段管理接口 ====================

    @Operation(summary = "获取抢课时段")
    @GetMapping("/rush/window")
    public R<Map<String, Object>> getRushWindow() {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail("无权限访问");
        }
        String startTime = redisTemplate.opsForValue().get(rushConfig.getEnrollmentStartKey());
        String endTime = redisTemplate.opsForValue().get(rushConfig.getEnrollmentEndKey());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("enabled", rushConfig.isEnrollmentEnabled(redisTemplate));
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("inWindow", rushConfig.isInRushWindow(redisTemplate));
        return R.ok(result);
    }

    @Operation(summary = "设置抢课时段")
    @PutMapping("/rush/window")
    public R<Void> setRushWindow(
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail("无权限访问");
        }
        if (startTime != null) {
            redisTemplate.opsForValue().set(rushConfig.getEnrollmentStartKey(), String.valueOf(startTime));
        } else {
            redisTemplate.delete(rushConfig.getEnrollmentStartKey());
        }
        if (endTime != null) {
            redisTemplate.opsForValue().set(rushConfig.getEnrollmentEndKey(), String.valueOf(endTime));
        } else {
            redisTemplate.delete(rushConfig.getEnrollmentEndKey());
        }
        log.info("抢课时段已设置: start={}, end={}", startTime, endTime);
        return R.ok();
    }

    @Operation(summary = "抢课状态概览（管理员）")
    @GetMapping("/rush/status")
    public R<Map<String, Object>> getRushStatus() {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail("无权限访问");
        }
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("enrollmentEnabled", rushConfig.isEnrollmentEnabled(redisTemplate));
        status.put("inRushWindow", rushConfig.isInRushWindow(redisTemplate));
        // 查询各课程的剩余名额实时统计
        return R.ok(status);
    }
}
