package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程实体。
 *
 * <p>对应数据库表 {@code course}，存储课程的基本信息。
 *
 * <p>课程与开课为一对多关系：一门课程可在不同学期多次开课。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {
    /** 课程编号（唯一标识） */
    private String courseNo;
    /** 课程名称 */
    private String name;
    /** 英文名称 */
    private String englishName;
    /** 所属学院 ID */
    private Long collegeId;
    /** 课程类别：1=必修, 2=选修, 3=公选, 4=实践 */
    private Integer category;
    /** 学分 */
    private java.math.BigDecimal credits;
    /** 总学时 */
    private Integer totalHours;
    /** 理论学时 */
    private Integer theoryHours;
    /** 实践学时 */
    private Integer practiceHours;
    /** 考核方式：1=考试, 2=考查 */
    private Integer examType;
    /** 课程描述 */
    private String description;
    /** 先修课程（多个用逗号分隔） */
    private String prerequisite;
    /** 最大选课人数限制 */
    private Integer maxStudents;
    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
