package com.ais.evaluation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 教学评价结果实体。
 *
 * <p>对应数据库表 {@code evaluation_result}，存储学生对教师的评价结果。
 */
@Data
@TableName("evaluation_result")
public class EvaluationResult {
    /** 主键 ID */
    private Long id;
    /** 关联的问卷 ID */
    private Long questionnaireId;
    /** 评价学生 ID（关联 student 表） */
    private Long studentId;
    /** 评分（1-5分） */
    private Integer score;
    /** 评价留言 */
    private String comment;
}
