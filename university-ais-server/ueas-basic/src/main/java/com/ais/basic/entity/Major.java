package com.ais.basic.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专业实体。
 *
 * <p>对应数据库表 {@code major}，存储各专业信息。
 *
 * <p>专业与学院为一对多关系：一个学院可下设多个专业。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("major")
public class Major extends BaseEntity {
    /** 专业名称 */
    private String name;
    /** 专业代码 */
    private String code;
    /** 所属学院 ID */
    private Long collegeId;
    /** 学位类型（如"工学"、"理学"） */
    private String degree;
    /** 学制（年） */
    private Integer years;
    /** 总学分要求 */
    private java.math.BigDecimal totalCredits;
    /** 状态：0=禁用, 1=启用 */
    private Integer status;
}
