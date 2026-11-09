package com.ais.system.controller;

import com.ais.common.result.R;
import com.ais.notice.entity.Notice;
import com.ais.notice.service.NoticeService;
import com.ais.security.util.SaTokenUtil;
import com.ais.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 系统配置控制器。
 *
 * <p>提供系统级配置的管理接口，仅管理员可访问。
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/sys-config")
@RequiredArgsConstructor
@Slf4j
public class SysConfigController {

    private final SysConfigService sysConfigService;
    private final NoticeService noticeService;

    /** 抢课开关键 */
    private static final String KEY_ENROLLMENT_ENABLED = "enrollment_enabled";

    @Operation(summary = "获取系统配置")
    @GetMapping
    public R<?> getConfig(@RequestParam String key) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail("无权限访问");
        }
        return R.ok(sysConfigService.get(key, null));
    }

    @Operation(summary = "设置系统配置")
    @PutMapping
    public R<Void> setConfig(@RequestParam String key, @RequestParam String value) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail("无权限访问");
        }
        sysConfigService.set(key, value);
        return R.ok();
    }

    @Operation(summary = "获取抢课开关状态")
    @GetMapping("/enrollment/enabled")
    public R<Boolean> getEnrollmentEnabled() {
        boolean enabled = sysConfigService.getBoolean(KEY_ENROLLMENT_ENABLED, false);
        return R.ok(enabled);
    }

    @Operation(summary = "设置抢课开关")
    @PutMapping("/enrollment/enabled")
    public R<Void> setEnrollmentEnabled(@RequestParam boolean enabled) {
        if (!SaTokenUtil.hasRole("ROLE_ADMIN")) {
            return R.fail("无权限访问");
        }
        sysConfigService.setBoolean(KEY_ENROLLMENT_ENABLED, enabled);
        if (enabled) {
            // 开启抢课时自动发送通知给全体用户
            createEnrollmentNotice();
        }
        return R.ok();
    }

    /**
     * 创建抢课开启通知，发送给全体用户。
     */
    private void createEnrollmentNotice() {
        try {
            Notice notice = new Notice();
            notice.setTitle("抢课功能已开启");
            notice.setContent("抢课通道已正式开放，请同学们尽快进入「我的选课→选择课程」完成选课。选课时间以教务处通知为准，请合理安排时间，避免错过。");
            notice.setType(2); // 教务通知
            notice.setPublisherId(SaTokenUtil.getCurrentUserId()); // 当前管理员ID
            notice.setTargetRole(null); // 面向全部用户
            notice.setStatus(1); // 已发布
            notice.setPublishTime(LocalDateTime.now());
            notice.setExpireTime(LocalDateTime.now().plusDays(7));
            notice.setTopFlag(1); // 置顶
            noticeService.save(notice);
            log.info("抢课开启通知已发送, publisherId={}", notice.getPublisherId());
        } catch (Exception e) {
            log.error("抢课通知创建失败: {}", e.getMessage());
        }
    }
}
