package com.ais.enrollment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 选课服务接口。
 *
 * <p>提供学生的选课、退课、查询已选课程和可选课程等功能。
 */
public interface EnrollmentService {
    /**
     * 查询学生已选课程列表（分页）。
     *
     * @param studentId   学生 ID
     * @param semesterId  学期 ID（可选）
     * @param current     当前页码
     * @param size        每页条数
     * @return 分页结果
     */
    IPage<Map<String, Object>> getMyCourses(Long studentId, Long semesterId, long current, long size);

    /**
     * 学生选修课程。
     *
     * @param studentId    学生 ID
     * @param courseOfferId 开课 ID
     * @throws BizException 当课程不存在、已选满或时间冲突时抛出
     */
    void selectCourse(Long studentId, Long courseOfferId);

    /**
     * 学生退选课程。
     *
     * @param studentId    学生 ID
     * @param courseOfferId 开课 ID
     * @throws BizException 当找不到选课记录时抛出
     */
    void dropCourse(Long studentId, Long courseOfferId);

    /**
     * 查询可选课程列表（不包含已选课程）。
     *
     * @param studentId   学生 ID
     * @param semesterId  学期 ID
     * @return 可选课程列表
     */
    List<Map<String, Object>> getAvailableCourses(Long studentId, Long semesterId);

    /**
     * 查询选课名单。
     *
     * @param courseOfferId 开课 ID
     * @return 选课学生列表
     */
    List<Map<String, Object>> getEnrollmentRoster(Long courseOfferId);
}
