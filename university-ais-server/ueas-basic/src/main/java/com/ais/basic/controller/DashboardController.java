package com.ais.basic.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.ais.basic.service.DashboardService;
import com.ais.basic.util.UserContextUtil;
import com.ais.common.result.R;
import com.ais.basic.dto.DashboardStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板控制器。
 */
@Tag(name = "数据看板")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserContextUtil userContextUtil;

    @Operation(summary = "获取首页统计数据")
    @SaIgnore
    @GetMapping("/stats")
    public R<DashboardStats> getStats() {
        Integer userType = userContextUtil.getUserType();
        return R.ok(dashboardService.getDashboardStats(userType));
    }
}
