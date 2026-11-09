package com.ais.basic.controller;

import com.ais.basic.entity.Student;
import com.ais.basic.service.StudentService;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生管理控制器。
 *
 * <p>提供学生的增删改查接口，支持分页查询与下拉列表（limit 500）。
 */
@Tag(name = "学生管理")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "查询学生列表（下拉框用）")
    @GetMapping("/list")
    public R<java.util.List<Student>> list(Map<String, Object> params) {
        int limit = Math.min(500, params.containsKey("size") ? ((Number) params.get("size")).intValue() : 500);
        return R.ok(studentService.pageQuery(params, 1, limit).getList());
    }

    @Operation(summary = "分页查询学生列表")
    @GetMapping("/page")
    public R<PageResult<Student>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(studentService.pageQuery(params, current, size));
    }

    @Operation(summary = "根据ID查询学生详情")
    @GetMapping("/{id}")
    public R<Student> getById(@PathVariable Long id) {
        Student student = studentService.getDetailById(id);
        if (student == null) {
            return R.fail(ResultCode.NOT_FOUND);
        }
        return R.ok(student);
    }

    @Operation(summary = "新增学生")
    @PostMapping
    public R<Void> add(@RequestBody Student student) {
        if (student == null || student.getStudentNo() == null || student.getName() == null) {
            return R.fail("学号和姓名不能为空");
        }
        if (student.getCollegeId() == null) {
            return R.fail("学院不能为空");
        }
        if (student.getEnrollDate() == null) {
            student.setEnrollDate(java.time.LocalDate.now());
        }
        log.info("新增学生: studentNo={}", student.getStudentNo());
        boolean result = studentService.save(student);
        return result ? R.ok() : R.fail("学生添加失败");
    }

    @Operation(summary = "修改学生信息")
    @PutMapping
    public R<Void> update(@RequestBody Student student) {
        if (student == null || student.getId() == null) {
            return R.fail("学生ID不能为空");
        }
        log.info("更新学生: id={}", student.getId()); studentService.updateById(student);
        return R.ok();
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除学生: id={}", id); studentService.removeById(id);
        return R.ok();
    }
}
