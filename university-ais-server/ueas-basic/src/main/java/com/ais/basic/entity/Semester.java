package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学期实体。
 *
 * <p>对应数据库表 {@code semester}，存储学年学期信息。
 *
 * <p>学期状态（status）：
 * <ul>
 *   <li>0 — 未开始</li>
 *   <li>1 — 选课中</li>
 *   <li>2 — 教学中</li>
 *   <li>3 — 已结束</li>
 * </ul>
 *
 * <p>选课时间范围（selectStart ~ selectEnd）用于控制学生选课开关。
 */
@Data
@TableName("semester")
public class Semester extends BaseEntity {

    /** 学期名称（如 2025-2026学年第二学期） */
    private String name;

    /** 学期代码（如 2025-2） */
    private String code;

    /** 学期开始日期 */
    private java.time.LocalDate startDate;

    /** 学期结束日期 */
    private java.time.LocalDate endDate;

    /** 选课开始时间 */
    private java.time.LocalDateTime selectStart;

    /** 选课结束时间 */
    private java.time.LocalDateTime selectEnd;

    /** 学期状态：0=未开始, 1=选课中, 2=教学中, 3=已结束 */
    private Integer status;
}
