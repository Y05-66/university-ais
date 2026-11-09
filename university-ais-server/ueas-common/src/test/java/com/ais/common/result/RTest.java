package com.ais.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一响应封装 {@link R} 的冒烟测试。
 */
class RTest {

    @Test
    void ok_noData_returnsStandardResponse() {
        R<String> result = R.ok();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void okWithData_returnsWrappedValue() {
        String payload = "hello";
        R<String> result = R.ok(payload);
        assertEquals(200, result.getCode());
        assertEquals(payload, result.getData());
    }

    @Test
    void failWithString_sets500AndMessage() {
        R<Void> result = R.fail("something wrong");
        assertEquals(500, result.getCode());
        assertEquals("something wrong", result.getMsg());
    }

    @Test
    void failWithCodeAndMsg_usesProvidedValues() {
        R<Void> result = R.fail(404, "not found");
        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMsg());
    }

    @Test
    void failWithResultCode_usesEnumFields() {
        R<Void> result = R.fail(ResultCode.USER_NOT_FOUND);
        assertEquals(1001, result.getCode());
        assertEquals("用户不存在", result.getMsg());
    }
}
