package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生选课记录实体。
 *
 * <p>对应数据库表 {@code student_course}，记录学生的选课及成绩信息。
 *
 * <p>与开课表（course_offer）关联，一个选课记录对应一个开课。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_course")
public class StudentCourse extends BaseEntity {
    /** 主键 ID */
    private Long id;
    /** 学生 ID（关联 student 表） */
    private Long studentId;
    /** 开课 ID（关联 course_offer 表） */
    private Long courseOfferId;
    /** 学期 ID */
    private Long semesterId;
    /** 选课时间 */
    private java.time.LocalDateTime selectTime;
    /** 选课状态：0=已退课, 1=已选 */
    private Integer status;
    /** 成绩（0-100） */
    private java.math.BigDecimal score;
    /** 绩点 */
    private java.math.BigDecimal gradePoint;
    /** 成绩状态：0=未录入, 1=已录入 */
    private Integer scoreStatus;
    /** 成绩修改次数（最多允许修改3次） */
    private Integer scoreModifyCount;
    /** 课程编号（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String courseNo;
    /** 课程名称（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String courseName;
    /** 学分（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private java.math.BigDecimal credits;
    /** 授课教师姓名（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String teacherName;
    /** 上课时间（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String schedule;
    /** 上课周次（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String weeks;
    /** 上课教室（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String classroom;
}
