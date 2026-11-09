package com.ais.notice.service;

import com.ais.common.result.PageResult;
import com.ais.notice.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 通知公告服务接口。
 *
 * <p>提供公告的分页查询和过期清理功能。
 */
public interface NoticeService extends IService<Notice> {
    /**
     * 分页查询公告列表（自动过滤已过期公告）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<Notice> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 根据用户角色过滤通知。
     * targetRole 为空时返回全部，否则只返回匹配的通知。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    default PageResult<Notice> pageQueryByRole(Map<String, Object> params, long current, long size) {
        String targetRole = (String) params.get("targetRole");
        if (targetRole != null && !targetRole.isEmpty()) {
            params.put("targetRoleEq", targetRole);
        }
        return pageQuery(params, current, size);
    }

    /**
     * 清理已过期的通知（软删除）。
     *
     * @return 删除的记录数
     */
    int cleanupExpiredNotices();
}
