package com.ais.enrollment.controller;

import com.ais.basic.entity.CourseOffer;
import com.ais.basic.entity.Teacher;
import com.ais.basic.mapper.TeacherMapper;
import com.ais.basic.util.UserContextUtil;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.enrollment.service.CourseOfferService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 开课管理控制器。
 *
 * <p>提供开课记录的分页查询、列表查询及增删改接口，
 * 并提供当前登录教师/学生所授/所选课程的查询接口。
 */
@Tag(name = "开课管理")
@RestController
@RequestMapping("/course-offer")
@RequiredArgsConstructor
public class CourseOfferController {

    private final CourseOfferService courseOfferService;
    private final TeacherMapper teacherMapper;
    private final UserContextUtil userContextUtil;

    @Operation(summary = "查询开课列表")
    @GetMapping("/list")
    public R<List<CourseOffer>> list(Map<String, Object> params) {
        return R.ok(courseOfferService.pageQuery(params, 1, 1000).getList());
    }

    @Operation(summary = "分页查询开课列表")
    @GetMapping("/page")
    public R<PageResult<CourseOffer>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(courseOfferService.pageQuery(params, current, size));
    }

    @Operation(summary = "新增开课")
    @PostMapping
    public R<Void> add(@RequestBody CourseOffer offer) {
        if (offer == null || offer.getCourseId() == null || offer.getTeacherId() == null || offer.getSemesterId() == null) {
            return R.fail("课程、教师和学期不能为空");
        }
        courseOfferService.save(offer);
        return R.ok();
    }

    @Operation(summary = "修改开课")
    @PutMapping
    public R<Void> update(@RequestBody CourseOffer offer) {
        courseOfferService.updateById(offer);
        return R.ok();
    }

    @Operation(summary = "删除开课")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        courseOfferService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "我的授课列表")
    @GetMapping("/my")
    public R<PageResult<CourseOffer>> myTeaching(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        List<Teacher> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getUserId, userId)
                        .eq(Teacher::getDeleted, 0)
        );
        if (teachers == null || teachers.isEmpty()) {
            return R.ok(PageResult.of(new java.util.ArrayList<>(), 0, current, size));
        }
        return R.ok(courseOfferService.myTeachingCourses(teachers.get(0).getId(), current, size));
    }

    @Operation(summary = "我的课表（教师显示授课课表，学生显示选课课表）")
    @GetMapping("/timetable")
    public R<List<Map<String, Object>>> timetable() {
        Integer userType = userContextUtil.getUserType();
        if (userType == null) {
            return R.ok(new java.util.ArrayList<>());
        }
        // 教师：返回自己授课的课表
        if (userType == 2) {
            Long userId = userContextUtil.getCurrentUserId();
            List<Teacher> teachers = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, userId)
                            .eq(Teacher::getDeleted, 0)
            );
            if (teachers == null || teachers.isEmpty()) {
                return R.ok(new java.util.ArrayList<>());
            }
            return R.ok(courseOfferService.getTimetable(teachers.get(0).getId()));
        }
        // 学生：返回自己选课的课表
        if (userType == 1) {
            Long studentId = userContextUtil.getStudentId();
            if (studentId == null) {
                return R.ok(new java.util.ArrayList<>());
            }
            return R.ok(courseOfferService.getTimetableByStudent(studentId, null));
        }
        return R.ok(new java.util.ArrayList<>());
    }

    @Operation(summary = "课表查询（管理员查询任意教师或学生）")
    @GetMapping("/timetable-query")
    public R<List<Map<String, Object>>> timetableQuery(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long semesterId) {
        if (teacherId != null) {
            return R.ok(courseOfferService.getTimetableByTeacherAndSemester(teacherId, semesterId));
        } else if (studentId != null) {
            return R.ok(courseOfferService.getTimetableByStudent(studentId, semesterId));
        }
        return R.ok(new java.util.ArrayList<>());
    }
}
