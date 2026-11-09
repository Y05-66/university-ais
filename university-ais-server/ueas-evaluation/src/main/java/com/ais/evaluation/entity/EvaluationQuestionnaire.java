package com.ais.evaluation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 教学评价问卷实体。
 *
 * <p>对应数据库表 {@code evaluation_questionnaire}，存储教师教学评价的问卷信息。
 */
@Data
@TableName("evaluation_questionnaire")
public class EvaluationQuestionnaire {
    /** 主键 ID */
    private Long id;
    /** 问卷名称 */
    private String name;
    /** 问卷描述 */
    private String description;
    /** 被评价教师 ID（关联 teacher 表） */
    private Long teacherId;
    /** 关联的开课 ID（关联 course_offer 表） */
    private Long courseOfferId;
    /** 评价开始时间 */
    private java.time.LocalDateTime startTime;
    /** 评价结束时间 */
    private java.time.LocalDateTime endTime;
    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
