package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学院实体。
 *
 * <p>对应数据库表 {@code college}，存储学校的二级学院信息。
 *
 * <p>用于：
 * <ul>
 *   <li>学院管理页面的增删改查</li>
 *   <li>专业管理中选择所属学院</li>
 *   <li>学生/教师管理中按学院筛选</li>
 * </ul>
 */
@Data
@TableName("college")
public class College extends BaseEntity {

    /** 学院名称 */
    private String name;

    /** 学院代码 */
    private String code;

    /** 院长姓名 */
    private String dean;

    /** 联系电话 */
    private String phone;

    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
