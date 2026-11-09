package com.ais.common.entity;

import lombok.Data;

/**
 * 实体基类，所有数据库实体均继承此类。
 *
 * <p>提供通用字段和自动填充功能：
 * <ul>
 *   <li>{@code id} — 主键，自增策略</li>
 *   <li>{@code createTime} — 创建时间，由 MyBatisPlusMetaHandler 自动填充</li>
 *   <li>{@code updateTime} — 更新时间，插入和更新时自动填充</li>
 *   <li>{@code deleted} — 逻辑删除标记，0=未删除，1=已删除</li>
 * </ul>
 */
@Data
public abstract class BaseEntity {

    /** 主键 ID，数据库自增 */
    private Long id;

    /** 创建时间，由 MyBatisPlusMetaHandler 在插入时自动填充 */
    private java.time.LocalDateTime createTime;

    /** 更新时间，由 MyBatisPlusMetaHandler 在插入和更新时自动填充 */
    private java.time.LocalDateTime updateTime;

    /** 逻辑删除标记：0=未删除（正常），1=已删除（已删除） */
    private Integer deleted;
}
