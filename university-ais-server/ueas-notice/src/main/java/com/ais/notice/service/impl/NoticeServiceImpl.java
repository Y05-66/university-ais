package com.ais.notice.service.impl;

import com.ais.common.result.PageResult;
import com.ais.notice.entity.Notice;
import com.ais.notice.mapper.NoticeMapper;
import com.ais.notice.service.NoticeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知公告服务实现类。
 *
 * <p>提供公告的分页查询、增删改操作和过期清理功能。
 * 查询时自动过滤已过期的公告。
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public PageResult<Notice> pageQuery(Map<String, Object> params, long current, long size) {
        IPage<Notice> page = baseMapper.selectPage(new Page<>(current, size), buildWrapper(params));
        return PageResult.of(page);
    }

    @Override
    public boolean save(Notice notice) {
        // 如果已发布且未设置过期时间，自动设置为3天后
        if (notice.getStatus() != null && notice.getStatus() == 1 && notice.getExpireTime() == null) {
            notice.setExpireTime(LocalDateTime.now().plusDays(3));
        }
        return super.save(notice);
    }

    @Override
    public boolean updateById(Notice notice) {
        // 如果更新为已发布状态且未设置过期时间
        if (notice.getStatus() != null && notice.getStatus() == 1 && notice.getExpireTime() == null) {
            notice.setExpireTime(LocalDateTime.now().plusDays(3));
        }
        return super.updateById(notice);
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 过期通知清理：删除已过期且未删除的通知
     */
    @Override
    public int cleanupExpiredNotices() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1) // 已发布
               .lt(Notice::getExpireTime, LocalDateTime.now()) // 已过期
               .eq(Notice::getDeleted, 0); // 未删除
        return baseMapper.delete(wrapper);
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notice> buildWrapper(Map<String, Object> params) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notice> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        w.like(StringUtils.hasText((String) params.get("title")), Notice::getTitle, params.get("title"));
        w.eq(params.get("type") != null, Notice::getType, params.get("type"));
        w.eq(params.get("status") != null, Notice::getStatus, params.get("status"));
        // 查询时排除已过期的通知
        w.and(wrapper -> wrapper
                .isNull(Notice::getExpireTime)
                .or()
                .ge(Notice::getExpireTime, LocalDateTime.now())
        );
        w.eq(params.get("targetRoleEq") != null, Notice::getTargetRole, params.get("targetRoleEq"));
        w.orderByDesc(Notice::getTopFlag).orderByDesc(Notice::getPublishTime);
        return w;
    }
}
