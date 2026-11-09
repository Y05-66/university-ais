package com.ais.system.controller;

import com.ais.common.result.R;
import com.ais.system.entity.DictData;
import com.ais.system.mapper.DictDataMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器。
 *
 * <p>提供字典数据的增删改查接口，支持按类型查询。
 */
@Tag(name = "数据字典")
@RestController
@RequestMapping("/sys-dict")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataMapper dictDataMapper;

    @Operation(summary = "根据类型查询字典数据")
    @GetMapping("/type/{dictType}")
    public R<List<DictData>> getByType(@PathVariable String dictType) {
        return R.ok(dictDataMapper.selectByType(dictType));
    }

    @Operation(summary = "查询所有字典")
    @GetMapping("/list")
    public R<List<DictData>> list() {
        return R.ok(dictDataMapper.selectList(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDeleted, 0)));
    }

    @Operation(summary = "新增字典")
    @PostMapping
    public R<Void> add(@RequestBody DictData data) {
        dictDataMapper.insert(data);
        return R.ok();
    }

    @Operation(summary = "修改字典")
    @PutMapping
    public R<Void> update(@RequestBody DictData data) {
        dictDataMapper.updateById(data);
        return R.ok();
    }

    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dictDataMapper.deleteById(id);
        return R.ok();
    }
}
