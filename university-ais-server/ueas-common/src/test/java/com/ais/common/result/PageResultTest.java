package com.ais.common.result;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分页结果封装 {@link PageResult} 的冒烟测试。
 */
class PageResultTest {

    @Test
    void of_returnsPageResult() {
        List<String> list = Arrays.asList("a", "b", "c");
        PageResult<String> result = PageResult.of(list, 10L, 1L, 3L);

        assertEquals(3, result.getList().size());
        assertEquals(10L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(3L, result.getSize());
    }

    @Test
    void emptyList_returnsEmptyResult() {
        PageResult<String> result = PageResult.of(Collections.emptyList(), 0L, 1L, 10L);

        assertTrue(result.getList().isEmpty());
        assertEquals(0L, result.getTotal());
    }
}
