package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师实体。
 *
 * <p>对应数据库表 {@code teacher}，存储教师基本信息。
 *
 * <p>与教师用户表（sys_user）一对一关联：通过 userId 字段关联。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teacher")
public class Teacher extends BaseEntity {
    /** 关联的系统用户 ID */
    private Long userId;
    /** 工号（唯一标识） */
    private String teacherNo;
    /** 姓名 */
    private String name;
    /** 性别：1=男, 2=女 */
    private Integer gender;
    /** 所属学院 ID */
    private Long collegeId;
    /** 职称（如"教授"、"副教授"、"讲师"、"助教"） */
    private String title;
    /** 联系电话 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 办公室 */
    private String office;
    /** 状态：0=离校, 1=在职 */
    private Integer status;
}
