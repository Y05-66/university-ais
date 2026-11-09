package com.ais.score.controller;

import com.ais.score.vo.CourseStudentVo;
import com.ais.score.vo.StudentScoreInputVo;
import com.ais.basic.entity.CourseOffer;
import com.ais.basic.entity.StudentCourse;
import com.ais.basic.mapper.CourseOfferMapper;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.score.service.ScoreService;
import com.ais.security.util.SaTokenUtil;
import com.ais.basic.util.UserContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理控制器。
 *
 * <p>提供学生成绩查询、教师成绩录入、成绩审核等功能。
 * 核心权限控制：教师只能查看和录入自己所授课程的成绩，管理员可查看全部。
 */
@Tag(name = "成绩管理")
@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final CourseOfferMapper courseOfferMapper;
    private final UserContextUtil userContextUtil;

    @Operation(summary = "我的成绩")
    @GetMapping("/my")
    public R<PageResult<StudentCourse>> my(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long semesterId,
            Map<String, Object> params) {
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return R.ok(PageResult.of(List.of(), 0, current, size));
        }
        params.put("semesterId", semesterId);
        return R.ok(scoreService.getMyScores(studentId, params, current, size));
    }

    @Operation(summary = "教师录入成绩")
    @PostMapping("/input")
    public R<Void> input(@RequestBody StudentCourse studentCourse) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN") && !SaTokenUtil.hasRole("ROLE_TEACHER")) {
            return R.fail(ResultCode.UNAUTHORIZED);
        }
        StudentCourse existing = scoreService.getById(studentCourse.getId());
        if (existing == null) {
            return R.fail("选课记录不存在");
        }
        if (existing.getCourseOfferId() == null) {
            return R.fail("课程开课ID不能为空");
        }
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            CourseOffer offer = courseOfferMapper.selectById(existing.getCourseOfferId());
            if (offer == null || offer.getTeacherId() == null) {
                return R.fail("课程信息不存在");
            }
            List<Long> teacherIds = userContextUtil.getTeacherIds();
            if (teacherIds == null || !teacherIds.contains(offer.getTeacherId())) {
                return R.fail("无权录入该课程的成绩");
            }
        }
        scoreService.inputScore(studentCourse);
        return R.ok();
    }

    @Operation(summary = "成绩录入列表（按课程分组）")
    @GetMapping("/input/list")
    public R<List<CourseStudentVo>> scoreInputList(
            @RequestParam(required = false) Long semesterId) {
        List<Long> teacherIds = userContextUtil.getTeacherIds();
        if (teacherIds == null) {
            return R.ok(scoreService.getScoreInputList(List.of(0L), semesterId));
        }
        return R.ok(scoreService.getScoreInputList(teacherIds, semesterId));
    }

    @Operation(summary = "学员成绩录入列表（管理员/教师视图）")
    @GetMapping("/student/input/list")
    public R<List<StudentScoreInputVo>> studentScoreInputList(
            @RequestParam(required = false) Long semesterId) {
        // 管理员无需教师关联即可查看全部成绩
        List<Long> teacherIds = userContextUtil.getTeacherIds();
        if (teacherIds == null) {
            return R.ok(scoreService.getStudentScoreInputList(List.of(0L), semesterId));
        }
        return R.ok(scoreService.getStudentScoreInputList(teacherIds, semesterId));
    }

    @Operation(summary = "成绩查询（旧的按学生列表）")
    @GetMapping("/teacher")
    public R<PageResult<StudentCourse>> teacherScores(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long teacherId) {
        List<Long> teacherIds = userContextUtil.getTeacherIds();
        if (teacherIds == null && !SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.UNAUTHORIZED);
        }
        Long targetTeacherId = (teacherIds == null || teacherIds.isEmpty()) ? 0L : teacherIds.get(0);
        return R.ok(scoreService.getTeacherScores(targetTeacherId, semesterId, current, size));
    }

    @Operation(summary = "成绩审核")
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN") && !SaTokenUtil.hasRole("ROLE_ACADEMIC")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        scoreService.approveScore(id);
        return R.ok();
    }

    @Operation(summary = "成绩修改")
    @PutMapping("/{id}/change")
    public R<Void> change(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN") && !SaTokenUtil.hasRole("ROLE_ACADEMIC")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        if (body.get("score") == null || body.get("scoreStatus") == null) {
            return R.fail("成绩和状态不能为空");
        }
        BigDecimal newScore;
        Integer newStatus;
        try {
            newScore = new BigDecimal(body.get("score").toString());
            newStatus = Integer.parseInt(body.get("scoreStatus").toString());
        } catch (NumberFormatException e) {
            return R.fail("成绩和状态必须为有效数字");
        }
        if (newScore.compareTo(BigDecimal.ZERO) < 0 || newScore.compareTo(new BigDecimal(100)) > 0) {
            return R.fail("成绩必须在0-100之间");
        }
        if (newStatus != 0 && newStatus != 1) {
            return R.fail("状态必须为0或1");
        }
        scoreService.changeScore(id, newScore, newStatus);
        return R.ok();
    }

    @Operation(summary = "发布成绩")
    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN") && !SaTokenUtil.hasRole("ROLE_ACADEMIC")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        scoreService.publishScore(id);
        return R.ok();
    }
}
