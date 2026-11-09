package com.ais.basic.controller;

import com.ais.basic.entity.Semester;
import com.ais.basic.service.SemesterService;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学期管理控制器。
 *
 * <p>提供学期列表查询接口，供前端下拉框选择使用。
 */
@Tag(name = "学期管理")
@RestController
@RequestMapping("/basic/semester")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @Operation(summary = "查询学期列表")
    @GetMapping("/list")
    public R<List<Semester>> list() {
        return R.ok(semesterService.listAll());
    }

    @Operation(summary = "查询所有学期（含已禁用）")
    @GetMapping("/all")
    public R<List<Semester>> all() {
        return R.ok(semesterService.list());
    }
}
