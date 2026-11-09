package com.ais.exam.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监考安排实体。
 *
 * <p>对应数据库表 {@code exam_invigilate}，记录考试与监考教师的关联关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_invigilate")
public class ExamInvigilate extends BaseEntity {
    /** 关联的考试 ID */
    private Long examId;
    /** 监考教师 ID（关联 teacher 表） */
    private Long teacherId;
    /** 状态：0=待确认, 1=已确认 */
    private Integer status;
}
