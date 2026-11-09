package com.ais.system.controller;

import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.system.entity.SysLog;
import com.ais.system.mapper.SysLogMapper;
import com.ais.security.util.SaTokenUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志控制器。
 *
 * <p>提供系统操作日志的分页查询、单条删除与批量清理接口。
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/sys-log")
@RequiredArgsConstructor
public class SysLogController {

    /** 日志数据访问对象 */
    private final SysLogMapper logMapper;

    /**
     * 分页查询操作日志列表。
     *
     * @param current  当前页码
     * @param size     每页条数
     * @param username 用户名（可选，用于筛选）
     * @param status   状态（可选，0=失败, 1=成功）
     * @return 分页结果
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    public R<PageResult<SysLog>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> params = new HashMap<>();
        if (username != null && !username.isEmpty()) {
            params.put("username", username);
        }
        if (status != null) {
            params.put("status", status);
        }
        IPage<SysLog> page = logMapper.selectPage(new Page<>(current, size), params);
        return R.ok(PageResult.of(page));
    }

    /**
     * 删除指定 ID 的日志记录。
     *
     * @param id 日志 ID
     * @return 操作结果
     */
    @Operation(summary = "删除日志")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        logMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 清空 30 天前的过期日志。
     *
     * @return 操作结果
     */
    @Operation(summary = "清空日志")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        logMapper.deleteLogByDays();
        return R.ok();
    }
}
