package com.ais.notice.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告实体。
 *
 * <p>对应数据库表 {@code notice}，存储系统公告和通知信息。
 *
 * <p>公告类型（type）：
 * <ul>
 *   <li>1 — 系统公告</li>
 *   <li>2 — 教务通知</li>
 *   <li>3 — 选课通知</li>
 * </ul>
 *
 * <p>目标角色（targetRole）：
 * <ul>
 *   <li>空 — 面向全部用户</li>
 *   <li>student — 仅学生可见</li>
 *   <li>teacher — 仅教师可见</li>
 *   <li>admin — 仅管理员可见</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {
    /** 公告标题 */
    private String title;
    /** 公告正文内容 */
    private String content;
    /** 公告类型：1=系统公告, 2=教务通知, 3=选课通知 */
    private Integer type;
    /** 发布人用户 ID */
    private Long publisherId;
    /** 目标角色：admin/teacher/student，空=全部 */
    private String targetRole;
    /** 置顶标识：0=普通, 1=置顶 */
    private Integer topFlag;
    /** 状态：0=草稿, 1=已发布 */
    private Integer status;
    /** 发布时间 */
    private java.time.LocalDateTime publishTime;
    /** 过期时间，发布后3天自动失效 */
    private java.time.LocalDateTime expireTime;
}
