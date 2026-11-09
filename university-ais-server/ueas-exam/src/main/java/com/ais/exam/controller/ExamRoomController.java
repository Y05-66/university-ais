package com.ais.exam.controller;

import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.exam.entity.ExamRoom;
import com.ais.exam.service.ExamRoomService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考场管理控制器。
 *
 * <p>提供考场的增删改查接口，支持按教室名称模糊查询和座位数范围筛选。
 */
@Tag(name = "考场分配")
@RestController
@RequestMapping("/exam/room")
@RequiredArgsConstructor
public class ExamRoomController {

    private final ExamRoomService examRoomService;

    @Operation(summary = "分页查询考场列表")
    @GetMapping("/page")
    public R<PageResult<ExamRoom>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        LambdaQueryWrapper<ExamRoom> wrapper = new LambdaQueryWrapper<>();
        if (params.get("classroom") != null) {
            wrapper.like(ExamRoom::getClassroom, params.get("classroom"));
        }
        if (params.get("minSeat") != null) {
            wrapper.ge(ExamRoom::getSeatCount, params.get("minSeat"));
        }
        if (params.get("maxSeat") != null) {
            wrapper.le(ExamRoom::getSeatCount, params.get("maxSeat"));
        }
        wrapper.orderByDesc(ExamRoom::getCreateTime);
        Page<ExamRoom> page = new Page<>(current, size);
        return R.ok(PageResult.of(examRoomService.page(page, wrapper)));
    }

    @Operation(summary = "新增考场")
    @PostMapping
    public R<Void> add(@RequestBody ExamRoom room) {
        if (room == null || room.getClassroom() == null || room.getSeatCount() == null) {
            return R.fail("考场名称和座位数不能为空");
        }
        examRoomService.save(room);
        return R.ok();
    }

    @Operation(summary = "修改考场")
    @PutMapping
    public R<Void> update(@RequestBody ExamRoom room) {
        examRoomService.updateById(room);
        return R.ok();
    }

    @Operation(summary = "删除考场")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        examRoomService.removeById(id);
        return R.ok();
    }
}
