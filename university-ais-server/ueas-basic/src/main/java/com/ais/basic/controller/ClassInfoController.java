package com.ais.basic.controller;

import com.ais.basic.entity.ClassInfo;
import com.ais.basic.service.ClassInfoService;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.ais.common.result.PageResult;
import java.util.List;
import java.util.Map;

/**
 * 班级管理控制器。
 *
 * <p>提供班级信息的增删改查接口，支持列表查询和分页查询。
 */
@Tag(name = "班级管理")
@RestController
@RequestMapping("/basic/class")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    @Operation(summary = "查询班级列表")
    @GetMapping("/list")
    public R<List<ClassInfo>> list() {
        return R.ok(classInfoService.list());
    }

    @Operation(summary = "分页查询班级")
    @GetMapping("/page")
    public R<PageResult<ClassInfo>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(classInfoService.pageQuery(params, current, size));
    }

    @Operation(summary = "新增班级")
    @PostMapping
    public R<Void> add(@RequestBody ClassInfo classInfo) {
        if (classInfo == null || classInfo.getCode() == null || classInfo.getName() == null || classInfo.getMajorId() == null || classInfo.getGrade() == null) {
            return R.fail("班级代码、名称、所属专业和年级不能为空");
        }
        classInfoService.save(classInfo);
        return R.ok();
    }

    @Operation(summary = "修改班级")
    @PutMapping
    public R<Void> update(@RequestBody ClassInfo classInfo) {
        classInfoService.updateById(classInfo);
        return R.ok();
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        classInfoService.removeById(id);
        return R.ok();
    }
}
