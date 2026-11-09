package com.ais.basic.service;

import com.ais.basic.dto.DashboardStats;

/**
 * 数据看板服务接口。
 */
public interface DashboardService {
    /**
     * 获取首页统计数据，按角色区分。
     *
     * @param userType 用户类型：1=学生 2=教师 3=管理员
     * @return 仪表盘统计数据
     */
    DashboardStats getDashboardStats(Integer userType);
}
