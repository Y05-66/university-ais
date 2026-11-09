package com.ais.basic.controller;

import com.ais.basic.dto.TeacherCreateDTO;
import com.ais.basic.entity.Teacher;
import com.ais.basic.service.TeacherService;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 教师管理控制器。
 *
 * <p>提供教师的增删改查接口，支持分页查询与下拉列表（limit 500）。
 */
@Tag(name = "教师管理")
@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "查询教师列表")
    @GetMapping("/list")
    public R<java.util.List<Teacher>> list(Map<String, Object> params) {
        int limit = Math.min(500, params.containsKey("size") ? ((Number) params.get("size")).intValue() : 500);
        return R.ok(teacherService.pageQuery(params, 1, limit).getList());
    }

    @Operation(summary = "分页查询教师列表")
    @GetMapping("/page")
    public R<PageResult<Teacher>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(teacherService.pageQuery(params, current, size));
    }

    @Operation(summary = "新增教师")
    @PostMapping
    public R<Void> add(@Valid @RequestBody TeacherCreateDTO dto) {
        Teacher teacher = new Teacher();
        teacher.setTeacherNo(dto.getTeacherNo());
        teacher.setName(dto.getName());
        teacher.setCollegeId(dto.getCollegeId());
        boolean result = teacherService.save(teacher);
        return result ? R.ok() : R.fail("教师添加失败");
    }

    @Operation(summary = "修改教师信息")
    @PutMapping
    public R<Void> update(@RequestBody Teacher teacher) {
        if (teacher == null || teacher.getId() == null) {
            return R.fail("教师ID不能为空");
        }
        teacherService.updateById(teacher);
        return R.ok();
    }

    @Operation(summary = "删除教师")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        teacherService.removeById(id);
        return R.ok();
    }
}
