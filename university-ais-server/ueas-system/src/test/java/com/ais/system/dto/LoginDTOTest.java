package com.ais.system.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 登录 DTO {@link LoginDTO} 的冒烟测试。
 */
class LoginDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validLoginDTO_noErrors() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");
        dto.setCaptchaKey("abc123");
        dto.setCaptcha("abcd");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void emptyUsername_hasError() {
        LoginDTO dto = new LoginDTO();
        dto.setPassword("admin123");
        dto.setCaptchaKey("abc123");
        dto.setCaptcha("abcd");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void emptyPassword_hasError() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setCaptchaKey("abc123");
        dto.setCaptcha("abcd");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void emptyCaptchaKey_hasError() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");
        dto.setCaptcha("abcd");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("captchaKey")));
    }

    @Test
    void emptyCaptcha_hasError() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");
        dto.setCaptchaKey("abc123");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("captcha")));
    }
}
