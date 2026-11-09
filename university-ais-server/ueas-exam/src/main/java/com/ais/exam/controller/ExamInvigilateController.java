package com.ais.exam.controller;

import com.ais.basic.util.UserContextUtil;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.exam.entity.Exam;
import com.ais.exam.entity.ExamInvigilate;
import com.ais.exam.mapper.ExamMapper;
import com.ais.exam.service.ExamInvigilateService;
import com.ais.security.util.SaTokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监考安排控制器。
 *
 * <p>提供教师查看自己监考任务的接口，以及管理员对监考安排的增删改查。
 */
@Tag(name = "监考安排")
@RestController
@RequestMapping("/exam/invigilate")
@RequiredArgsConstructor
public class ExamInvigilateController {

    private final ExamInvigilateService examInvigilateService;
    private final ExamMapper examMapper;
    private final UserContextUtil userContextUtil;

    // ==================== 管理员接口 ====================

    @Operation(summary = "分页查询监考安排（含考试详情）")
    @GetMapping("/page")
    public R<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        LambdaQueryWrapper<ExamInvigilate> wrapper = new LambdaQueryWrapper<>();
        if (params.get("teacherId") != null) {
            wrapper.eq(ExamInvigilate::getTeacherId, params.get("teacherId"));
        }
        if (params.get("examId") != null) {
            wrapper.eq(ExamInvigilate::getExamId, params.get("examId"));
        }
        wrapper.orderByDesc(ExamInvigilate::getCreateTime);
        Page<ExamInvigilate> page = new Page<>(current, size);
        Page<ExamInvigilate> resultPage = examInvigilateService.page(page, wrapper);

        // 填充考试详情（批量查询避免 N+1）
        List<Long> examIds = resultPage.getRecords().stream()
                .map(ExamInvigilate::getExamId).distinct().toList();
        Map<Long, Exam> examMap = examIds.isEmpty() ? java.util.Collections.emptyMap()
                : examMapper.selectBatchIds(examIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Exam::getId, e -> e));

        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (ExamInvigilate inv : resultPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inv.getId());
            item.put("examId", inv.getExamId());
            item.put("teacherId", inv.getTeacherId());
            item.put("status", inv.getStatus());
            Exam e = examMap.get(inv.getExamId());
            if (e != null) {
                item.put("courseName", e.getCourseName());
                item.put("courseOfferId", e.getCourseOfferId());
                item.put("examDate", e.getExamDate());
                item.put("startTime", e.getStartTime());
                item.put("endTime", e.getEndTime());
                item.put("classroom", e.getClassroom());
                item.put("seatCount", e.getSeatCount());
            }
            list.add(item);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setCurrent(resultPage.getCurrent());
        pageResult.setSize(resultPage.getSize());
        return R.ok(pageResult);
    }

    @Operation(summary = "新增监考安排")
    @PostMapping
    public R<Void> add(@RequestBody ExamInvigilate invigilate) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        if (invigilate == null || invigilate.getExamId() == null
                || invigilate.getTeacherId() == null) {
            return R.fail("考试ID和教师ID不能为空");
        }
        examInvigilateService.save(invigilate);
        return R.ok();
    }

    @Operation(summary = "修改监考安排")
    @PutMapping
    public R<Void> update(@RequestBody ExamInvigilate invigilate) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        examInvigilateService.updateById(invigilate);
        return R.ok();
    }

    @Operation(summary = "删除监考安排")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        examInvigilateService.removeById(id);
        return R.ok();
    }

    // ==================== 教师接口 ====================

    @Operation(summary = "查询教师的监考安排（含考试详情）")
    @GetMapping("/my")
    public R<List<Map<String, Object>>> myInvigilation() {
        Integer userType = userContextUtil.getUserType();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        if (userType != null && userType == 2) {
            Long userId = userContextUtil.getCurrentUserId();
            if (userId != null) {
                List<Long> teacherIds = userContextUtil.getTeacherIds();
                LambdaQueryWrapper<ExamInvigilate> wrapper = new LambdaQueryWrapper<>();
                if (teacherIds != null && !teacherIds.isEmpty()) {
                    wrapper.eq(ExamInvigilate::getTeacherId, teacherIds.get(0));
                } else {
                    wrapper.eq(ExamInvigilate::getTeacherId, -1L);
                }
                List<ExamInvigilate> invigilations = examInvigilateService.list(wrapper);

                // 批量查询考试详情
                List<Long> invExamIds = invigilations.stream().map(ExamInvigilate::getExamId).distinct().toList();
                Map<Long, Exam> invExamMap = invExamIds.isEmpty() ? java.util.Collections.emptyMap()
                        : examMapper.selectBatchIds(invExamIds).stream()
                                .collect(java.util.stream.Collectors.toMap(Exam::getId, e -> e));

                for (ExamInvigilate inv : invigilations) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", inv.getId());
                    item.put("examId", inv.getExamId());
                    Exam e = invExamMap.get(inv.getExamId());
                    if (e != null) {
                        item.put("courseName", e.getCourseName());
                        item.put("examDate", e.getExamDate());
                        item.put("startTime", e.getStartTime());
                        item.put("endTime", e.getEndTime());
                        item.put("classroom", e.getClassroom());
                        item.put("seatCount", e.getSeatCount());
                    }
                    result.add(item);
                }
            }
        }
        return R.ok(result);
    }
}
