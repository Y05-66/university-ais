package com.ais.system.entity;

import com.ais.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 字典数据实体。
 *
 * <p>对应数据库表 {@code dict_data}，存储系统数据字典的键值对信息。
 *
 * <p>字典数据用于前端下拉框等静态数据展示，如性别、状态等枚举值。
 */
@Data
@TableName("dict_data")
public class DictData extends BaseEntity {
    /** 字典类型（如 sys_user_type、sys_status） */
    private String dictType;
    /** 字典标签（显示文本） */
    private String label;
    /** 字典值（存储值） */
    private String value;
    /** 排序号，数字越小越靠前 */
    private Integer sort;
    /** 状态：0=禁用, 1=启用 */
    private Integer status;
    /** 备注说明 */
    private String remark;
}
