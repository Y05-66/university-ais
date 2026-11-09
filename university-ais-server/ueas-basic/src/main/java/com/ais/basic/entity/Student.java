package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生实体。
 *
 * <p>对应数据库表 {@code student}，存储学生基本信息。
 *
 * <p>与学生用户表（sys_user）一对一关联：通过 userId 字段关联。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {
    /** 关联的系统用户 ID */
    private Long userId;
    /** 学号（唯一标识） */
    private String studentNo;
    /** 姓名 */
    private String name;
    /** 性别：1=男, 2=女 */
    private Integer gender;
    /** 出生日期 */
    private java.time.LocalDate birthDate;
    /** 身份证号 */
    private String idCard;
    /** 所属学院 ID */
    private Long collegeId;
    /** 所属专业 ID */
    private Long majorId;
    /** 所属班级 ID */
    private Long classId;
    /** 班级名称（JOIN 查询填充，非数据库字段） */
    @TableField(exist = false)
    private String className;
    /** 年级（如"2024"） */
    private String grade;
    /** 入学日期 */
    private java.time.LocalDate enrollDate;
    /** 状态：0=休学, 1=在读, 2=毕业 */
    private Integer status;
    /** GPA（平均绩点） */
    private java.math.BigDecimal gpa;
    /** 已获得总学分 */
    private java.math.BigDecimal totalCredits;
}
