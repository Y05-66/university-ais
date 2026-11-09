package com.ais.basic.controller;

import com.ais.basic.dto.MajorCreateDTO;
import com.ais.basic.entity.Major;
import com.ais.basic.service.MajorService;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 专业管理控制器。
 *
 * <p>提供专业的增删改查接口，支持分页查询。专业隶属于学院，是学生管理的基础数据。
 */
@Tag(name = "专业管理")
@RestController
@RequestMapping("/basic/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @Operation(summary = "分页查询专业")
    @GetMapping("/page")
    public R<PageResult<Major>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(majorService.pageQuery(params, current, size));
    }

    @Operation(summary = "新增专业")
    @PostMapping
    public R<Void> add(@Valid @RequestBody MajorCreateDTO dto) {
        Major major = new Major();
        major.setName(dto.getName());
        major.setCode(dto.getCode());
        major.setCollegeId(dto.getCollegeId());
        majorService.save(major);
        return R.ok();
    }

    @Operation(summary = "修改专业")
    @PutMapping
    public R<Void> update(@RequestBody Major major) {
        if (major == null || major.getId() == null) {
            return R.fail("专业ID不能为空");
        }
        majorService.updateById(major);
        return R.ok();
    }

    @Operation(summary = "删除专业")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        majorService.removeById(id);
        return R.ok();
    }
}
