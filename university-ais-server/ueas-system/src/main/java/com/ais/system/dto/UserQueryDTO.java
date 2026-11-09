package com.ais.system.dto;

import lombok.Data;

/**
 * 用户分页查询请求 DTO。
 *
 * <p>继承 SysUser 实体以支持按用户属性筛选，同时包含分页参数。
 */
@Data
public class UserQueryDTO extends com.ais.system.entity.SysUser {
    /** 当前页码（默认第 1 页） */
    private Long current = 1L;
    /** 每页条数（默认 20 条） */
    private Long size = 20L;
}
