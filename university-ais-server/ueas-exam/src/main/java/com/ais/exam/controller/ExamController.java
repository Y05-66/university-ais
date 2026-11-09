package com.ais.exam.controller;

import com.ais.basic.util.UserContextUtil;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.exam.entity.Exam;
import com.ais.exam.service.ExamService;
import com.ais.security.util.SaTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考试安排控制器。
 *
 * <p>提供考试安排的分页查询与增删改接口。
 */
@Tag(name = "考试管理")
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final UserContextUtil userContextUtil;

    @Operation(summary = "分页查询考试安排")
    @GetMapping("/page")
    public R<PageResult<Exam>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        // 教师只能看到自己被安排监考的考试
        Integer userType = userContextUtil.getUserType();
        if (userType != null && userType == 2) {
            Long userId = userContextUtil.getCurrentUserId();
            if (userId != null) {
                List<Long> teacherIds = userContextUtil.getTeacherIds();
                if (teacherIds != null && !teacherIds.isEmpty()) {
                    List<Long> examIds = examService.getExamIdsByTeacherId(teacherIds.get(0));
                    // 如果没有被安排任何监考任务，返回空列表
                    if (examIds.isEmpty()) {
                        params.put("_emptyFilter", true);
                    } else {
                        params.put("examIds", examIds);
                    }
                } else {
                    params.put("_emptyFilter", true);
                }
            }
        }
        return R.ok(examService.pageQuery(params, current, size));
    }

    @Operation(summary = "查询当前用户的考试安排（学生/教师）")
    @GetMapping("/my")
    public R<List<Map<String, Object>>> myExams() {
        Integer userType = userContextUtil.getUserType();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        if (userType == null) {
            return R.ok(result);
        }

        if (userType == 1) {
            // 学生：显示自己选课的考试
            Long studentId = userContextUtil.getStudentId();
            if (studentId != null) {
                result = examService.getMyExamSchedule(studentId);
            }
        } else if (userType == 2) {
            // 教师：显示自己被安排监考的考试
            Long userId = userContextUtil.getCurrentUserId();
            if (userId != null) {
                List<Long> teacherIds = userContextUtil.getTeacherIds();
                if (teacherIds != null && !teacherIds.isEmpty()) {
                    List<Long> examIds = examService.getExamIdsByTeacherId(teacherIds.get(0));
                    if (!examIds.isEmpty()) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("examIds", examIds);
                        PageResult<Exam> pageResult = examService.pageQuery(params, 1, 100);
                        result = pageResult.getList().stream().map(exam -> {
                            Map<String, Object> item = new HashMap<>();
                            item.put("courseName", exam.getCourseName());
                            item.put("examDate", exam.getExamDate());
                            item.put("startTime", exam.getStartTime());
                            item.put("endTime", exam.getEndTime());
                            item.put("classroom", exam.getClassroom());
                            item.put("seatCount", exam.getSeatCount());
                            return item;
                        }).collect(java.util.stream.Collectors.toList());
                    }
                }
            }
        }

        return R.ok(result);
    }

    @Operation(summary = "新增考试安排")
    @PostMapping
    public R<Void> add(@RequestBody Exam exam) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        if (exam == null || exam.getCourseOfferId() == null || exam.getExamDate() == null || exam.getClassroom() == null) {
            return R.fail("开课、考试日期和考场不能为空");
        }
        // 验证开课记录存在
        if (examService.pageQuery(java.util.Collections.emptyMap(), 1, 1).getList().stream()
                .noneMatch(e -> e.getCourseOfferId().equals(exam.getCourseOfferId()))) {
            return R.fail("开课记录不存在");
        }
        examService.save(exam);
        return R.ok();
    }

    @Operation(summary = "修改考试安排")
    @PutMapping
    public R<Void> update(@RequestBody Exam exam) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        examService.updateById(exam);
        return R.ok();
    }

    @Operation(summary = "删除考试安排")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        examService.removeById(id);
        return R.ok();
    }
}
