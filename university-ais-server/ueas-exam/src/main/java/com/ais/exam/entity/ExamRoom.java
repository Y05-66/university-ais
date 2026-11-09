package com.ais.exam.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考场实体。
 *
 * <p>对应数据库表 {@code exam_room}，存储考场（教室）的基本信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_room")
public class ExamRoom extends BaseEntity {
    /** 教室名称/编号（如"A301"） */
    private String classroom;
    /** 座位数 */
    private Integer seatCount;
    /** 状态：0=停用, 1=使用中 */
    private Integer status;
}
