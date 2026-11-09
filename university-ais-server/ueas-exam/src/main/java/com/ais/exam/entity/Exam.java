package com.ais.exam.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试安排实体。
 *
 * <p>对应数据库表 {@code exam}，存储各开课的考试安排信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {
    /** 关联的开课 ID（关联 course_offer 表） */
    private Long courseOfferId;
    /** 考试类型：1=期末, 2=期中, 3=补考 */
    private Integer examType;
    /** 考试日期 */
    private java.time.LocalDate examDate;
    /** 考试开始时间 */
    private java.time.LocalTime startTime;
    /** 考试结束时间 */
    private java.time.LocalTime endTime;
    /** 考试教室 */
    private String classroom;
    /** 座位数 */
    private Integer seatCount;
    /** 监考人员 */
    private String invigilator;
    /** 状态：0=禁用, 1=启用 */
    private Integer status;
    /** 课程名称（JOIN 查询填充，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String courseName;
    /** 教师姓名（JOIN 查询填充，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String teacherName;
}
