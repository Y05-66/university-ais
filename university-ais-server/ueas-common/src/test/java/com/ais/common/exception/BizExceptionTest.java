package com.ais.common.exception;

import com.ais.common.result.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务异常类 {@link BizException} 的冒烟测试。
 */
class BizExceptionTest {

    @Test
    void throwsWithResultCode_setsCorrectCodeAndMessage() {
        BizException ex = new BizException(ResultCode.USER_NOT_FOUND);

        assertEquals(1001, ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void throwsWithIntAndString() {
        BizException ex = new BizException(500, "自定义错误");

        assertEquals(500, ex.getCode());
        assertEquals("自定义错误", ex.getMessage());
    }

    @Test
    void isRuntimeException_subclass() {
        assertInstanceOf(RuntimeException.class, new BizException(ResultCode.INTERNAL_ERROR));
    }
}
