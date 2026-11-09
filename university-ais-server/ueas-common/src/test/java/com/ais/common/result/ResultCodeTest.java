package com.ais.common.result;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 结果码枚举 {@link ResultCode} 的冒烟测试。
 */
class ResultCodeTest {

    @Test
    void values_areAllNonEmpty() {
        assertNotNull(ResultCode.values());
        assertTrue(ResultCode.values().length > 5, "Expected at least a few predefined codes");
    }

    @Test
    void successCode_hasCorrectCode() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals("success", ResultCode.SUCCESS.getMessage());
    }

    @Test
    void loginFailed_hasCorrectCode() {
        assertEquals(1006, ResultCode.LOGIN_FAILED.getCode());
    }

    @Test
    void noDuplicateCodes() {
        long distinctCount = Stream.of(ResultCode.values())
                .map(ResultCode::getCode)
                .distinct()
                .count();
        assertEquals((long) ResultCode.values().length, distinctCount,
                "Each ResultCode should have a unique code value");
    }

    @Test
    void internalError_is500() {
        assertEquals(500, ResultCode.INTERNAL_ERROR.getCode());
        assertEquals("系统内部错误", ResultCode.INTERNAL_ERROR.getMessage());
    }

    @Test
    void unauthorized_is401() {
        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
    }
}
