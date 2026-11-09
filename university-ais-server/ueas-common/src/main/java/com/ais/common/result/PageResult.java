package com.ais.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装。
 *
 * <p>包装 MyBatis-Plus 的 {@code IPage}，提供更简洁的分页数据结构。
 *
 * @param <T> 数据项类型
 */
@Data
public class PageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long current;

    /** 每页大小 */
    private long size;

    /** 构造分页结果 */
    public static <T> PageResult<T> of(List<T> list, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }

    /** 从 MyBatis-Plus IPage 构造 */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        return of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
