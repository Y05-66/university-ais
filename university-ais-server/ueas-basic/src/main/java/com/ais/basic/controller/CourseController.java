package com.ais.basic.controller;

import com.ais.basic.dto.CourseCreateDTO;
import com.ais.basic.entity.Course;
import com.ais.basic.service.CourseService;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程管理控制器。
 *
 * <p>提供课程的增删改查接口，支持下拉列表（限制最大数量）和分页查询两种模式。
 * 课程是教务管理的核心数据，关联开课、选课、成绩等业务。
 */
@Tag(name = "课程管理")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "查询课程列表")
    @GetMapping("/list")
    public R<java.util.List<Course>> list(Map<String, Object> params) {
        int limit = Math.min(500, params.containsKey("size") ? ((Number) params.get("size")).intValue() : 500);
        return R.ok(courseService.pageQuery(params, 1, limit).getList());
    }

    @Operation(summary = "分页查询课程列表")
    @GetMapping("/page")
    public R<PageResult<Course>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(courseService.pageQuery(params, current, size));
    }

    @Operation(summary = "新增课程")
    @PostMapping
    public R<Void> add(@Valid @RequestBody CourseCreateDTO dto) {
        Course course = new Course();
        course.setCourseNo(dto.getCourseNo());
        course.setName(dto.getName());
        course.setCollegeId(dto.getCollegeId());
        course.setCategory(dto.getCategory());
        course.setCredits(dto.getCredits());
        course.setTotalHours(dto.getTotalHours());
        courseService.save(course);
        return R.ok();
    }

    @Operation(summary = "修改课程")
    @PutMapping
    public R<Void> update(@RequestBody Course course) {
        if (course == null || course.getId() == null) {
            return R.fail("课程ID不能为空");
        }
        courseService.updateById(course);
        return R.ok();
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return R.ok();
    }
}
