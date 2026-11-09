package com.ais.notice.controller;

import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.common.result.ResultCode;
import com.ais.notice.entity.Notice;
import com.ais.notice.service.NoticeService;
import com.ais.security.util.SaTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知公告控制器。
 *
 * <p>提供公告的分页查询、详情获取、新增、修改和删除功能。
 * 管理员可管理全部公告，教师只能管理自己发布的公告。
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "分页查询公告列表")
    @GetMapping("/page")
    public R<PageResult<Notice>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(noticeService.pageQuery(params, current, size));
    }

    @Operation(summary = "根据ID查询公告详情")
    @GetMapping("/{id}")
    public R<Notice> getById(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return R.fail(ResultCode.NOT_FOUND);
        }
        return R.ok(notice);
    }

    @Operation(summary = "新增公告")
    @PostMapping
    public R<Void> add(@RequestBody Notice notice) {
        if (notice == null || notice.getTitle() == null || notice.getContent() == null || notice.getType() == null) {
            return R.fail("公告标题、内容和类型不能为空");
        }
        // 自动填充发布者ID（前端不传，由服务端从登录态获取）
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        if (currentUserId != null) {
            notice.setPublisherId(currentUserId);
        }
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        // 如果已发布，自动设置过期时间为3天后
        if (notice.getStatus() == 1 && notice.getExpireTime() == null) {
            notice.setExpireTime(java.time.LocalDateTime.now().plusDays(3));
        }
        log.info("新增公告: id={}", notice.getId()); noticeService.save(notice);
        return R.ok();
    }

    @Operation(summary = "修改公告")
    @PutMapping
    public R<Void> update(@RequestBody Notice notice) {
        if (notice == null || notice.getId() == null) {
            return R.fail("公告ID不能为空");
        }
        // 如果更新为已发布且未设置过期时间
        if (notice.getStatus() != null && notice.getStatus() == 1 && notice.getExpireTime() == null) {
            notice.setExpireTime(java.time.LocalDateTime.now().plusDays(3));
        }
        log.info("更新公告: id={}", notice.getId()); noticeService.updateById(notice);
        return R.ok();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除公告: id={}", id); noticeService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "我的通知（按当前用户角色过滤）")
    @GetMapping("/my")
    public R<PageResult<Notice>> myPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        // 根据当前用户角色过滤：ADMIN 看到全部，TEACHER 看到自己的，STUDENT 只看公告类型
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        if (currentUserId != null) {
            if (SaTokenUtil.hasRole("ROLE_ADMIN")) {
                params.put("publisherId", currentUserId);
            } else if (SaTokenUtil.hasRole("ROLE_TEACHER")) {
                params.put("publisherId", currentUserId);
            } else {
                // 学生只看 status=1（已发布）的通知
                params.put("status", 1);
            }
        }
        return R.ok(noticeService.pageQuery(params, current, size));
    }
}
