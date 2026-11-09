package com.ais.basic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 课程创建/更新 DTO，使用 Bean Validation 注解统一参数校验。
 */
@Data
public class CourseCreateDTO {
    @NotBlank(message = "课程编号不能为空")
    @Size(max = 20, message = "课程编号长度不能超过20")
    private String courseNo;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100")
    private String name;

    @NotNull(message = "开课学院不能为空")
    private Long collegeId;

    @NotNull(message = "课程类别不能为空")
    private Integer category;

    @NotNull(message = "学分不能为空")
    private java.math.BigDecimal credits;

    @NotNull(message = "总学时不能为空")
    private Integer totalHours;
}
