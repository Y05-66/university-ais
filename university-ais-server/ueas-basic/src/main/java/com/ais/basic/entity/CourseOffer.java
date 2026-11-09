package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开课实体。
 *
 * <p>对应数据库表 {@code course_offer}，表示一门课程在特定学期的具体开课安排。
 *
 * <p>与课程、教师、学期的关系：
 * <ul>
 *   <li>一门课程可对应多个开课（不同学期）</li>
 *   <li>一个开课由一名教师负责</li>
 *   <li>一个开课属于一个学期</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_offer")
public class CourseOffer extends BaseEntity {
    /** 关联的课程 ID */
    private Long courseId;
    /** 授课教师 ID（关联 teacher 表） */
    private Long teacherId;
    /** 所属学期 ID */
    private Long semesterId;
    /** 上课教室 */
    private String classroom;
    /** 上课时间（如"周一1-2节"） */
    private String schedule;
    /** 上课周次（如"1-16周"） */
    private String weeks;
    /** 已选课人数 */
    private Integer enrolled;
    /** 最大选课人数 */
    private Integer maxStudents;
    /** 状态：0=已取消, 1=正常 */
    private Integer status;
    /** 乐观锁版本号，用于并发选课安全 */
    @Version
    private Integer version;
    /** 课程编号（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String courseNo;
    /** 课程名称（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String courseName;
    /** 教师姓名（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String teacherName;
    /** 学期名称（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String semesterName;
    /** 学分（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private java.math.BigDecimal credits;
    /** 课程类别（JOIN 查询填充，非数据库字段）1=必修 2=选修 3=公选 */
    @TableField(exist = false)
    private Integer category;
}
