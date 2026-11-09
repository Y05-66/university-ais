package com.ais.basic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 教师创建/更新 DTO，使用 Bean Validation 注解统一参数校验。
 */
@Data
public class TeacherCreateDTO {
    @NotBlank(message = "工号不能为空")
    @Size(max = 20, message = "工号长度不能超过20")
    private String teacherNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String name;

    @NotNull(message = "所属学院不能为空")
    private Long collegeId;
}
