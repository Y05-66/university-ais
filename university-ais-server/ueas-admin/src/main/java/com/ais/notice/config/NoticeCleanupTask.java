package com.ais.notice.config;

import com.ais.notice.service.NoticeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知定时清理任务。
 *
 * <p>每天凌晨2点执行，删除已过期的已发布通知（expireTime < 当前时间）。
 * 通知发布时自动设置过期时间为3天后。
 */
@Component
public class NoticeCleanupTask {

    private final NoticeService noticeService;

    public NoticeCleanupTask(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 每天凌晨2点清理过期通知。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredNotices() {
        int count = noticeService.cleanupExpiredNotices();
        if (count > 0) {
            System.out.println("[NoticeCleanup] 已清理 " + count + " 条过期通知");
        }
    }
}
