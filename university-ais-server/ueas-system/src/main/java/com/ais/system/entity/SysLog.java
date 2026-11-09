package com.ais.system.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统操作日志实体。
 *
 * <p>对应数据库表 {@code sys_log}，记录用户的操作日志信息，用于审计和追踪。
 *
 * <p>日志字段说明：
 * <ul>
 *   <li>{@code userId} — 操作人用户 ID</li>
 *   <li>{@code username} — 操作人用户名</li>
 *   <li>{@code operation} — 操作类型（如"新增"、"修改"、"删除"）</li>
 *   <li>{@code method} — 调用的方法全名</li>
 *   <li>{@code params} — 方法参数（JSON 格式）</li>
 *   <li>{@code ip} — 操作 IP 地址</li>
 *   <li>{@code duration} — 执行耗时（毫秒）</li>
 *   <li>{@code status} — 执行状态（0=失败，1=成功）</li>
 *   <li>{@code errorMsg} — 错误信息（失败时记录）</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_log")
public class SysLog extends BaseEntity {
    /** 操作人用户 ID */
    private Long userId;
    /** 操作人用户名 */
    private String username;
    /** 操作类型（如"新增用户"、"修改密码"） */
    private String operation;
    /** 调用的方法全限定名 */
    private String method;
    /** 方法参数（JSON 格式） */
    private String params;
    /** 操作 IP 地址 */
    private String ip;
    /** 执行耗时（毫秒） */
    private Integer duration;
    /** 执行状态：0=失败，1=成功 */
    private Integer status;
    /** 错误信息（失败时记录） */
    private String errorMsg;
}
