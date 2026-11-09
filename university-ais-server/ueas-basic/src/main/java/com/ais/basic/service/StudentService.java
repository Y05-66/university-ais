package com.ais.basic.service;

import com.ais.basic.entity.Student;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 学生服务接口。
 *
 * <p>提供学生的分页查询和详情查询功能。
 */
public interface StudentService extends IService<Student> {
    /**
     * 分页查询学生列表（支持按学号、姓名、学院、年级筛选）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<Student> pageQuery(Map<String, Object> params, long current, long size);
    /**
     * 根据 ID 查询学生详情（含班级、专业、学院名称）。
     *
     * @param id 学生 ID
     * @return 学生详情
     */
    Student getDetailById(Long id);
}
