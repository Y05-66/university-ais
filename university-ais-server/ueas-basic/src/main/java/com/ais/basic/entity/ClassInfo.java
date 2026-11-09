package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级实体。
 *
 * <p>对应数据库表 {@code class_info}，存储班级信息。
 *
 * <p>班级与专业为一对多关系：一个专业可下设多个班级。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_info")
public class ClassInfo extends BaseEntity {
    /** 班级名称（如"计科2024-1班"） */
    private String name;
    /** 班级代码（如"CS202401"） */
    private String code;
    /** 所属专业 ID */
    private Long majorId;
    /** 年级（如"2024"） */
    private String grade;
    /** 班主任 ID（关联 teacher 表） */
    private Long advisorId;
    /** 学生人数 */
    private Integer studentCount;
    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
