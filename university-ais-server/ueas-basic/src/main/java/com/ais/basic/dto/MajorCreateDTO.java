package com.ais.basic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 专业创建/更新 DTO，使用 Bean Validation 注解统一参数校验。
 */
@Data
public class MajorCreateDTO {
    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String name;

    @NotBlank(message = "专业代码不能为空")
    @Size(max = 20, message = "专业代码长度不能超过20")
    private String code;

    @NotNull(message = "所属学院不能为空")
    private Long collegeId;
}
