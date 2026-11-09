package com.ais.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器。
 *
 * <p>在实体插入和更新时自动填充以下字段，无需业务代码手动设置：
 * <ul>
 *   <li>createTime — 插入时设置为当前时间</li>
 *   <li>updateTime — 插入和更新时均设置为当前时间</li>
 *   <li>deleted — 插入时设置为 0（未删除）</li>
 * </ul>
 *
 * <p>配合 {@code BaseEntity} 中的 {@code @TableField(fill = FieldFill.xxx)} 注解使用。
 */
@Component
public class MyBatisPlusMetaHandler implements MetaObjectHandler {

    /**
     * 插入数据时自动填充字段。
     *
     * @param metaObject MyBatis 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    /**
     * 更新数据时自动填充字段。
     *
     * @param metaObject MyBatis 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
