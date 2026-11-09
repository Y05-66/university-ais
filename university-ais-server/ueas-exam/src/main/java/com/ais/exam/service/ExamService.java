package com.ais.exam.service;

import com.ais.common.result.PageResult;
import com.ais.exam.entity.Exam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 考试服务接口。
 *
 * <p>提供考试安排的 CRUD 操作和分页查询功能。
 */
public interface ExamService extends IService<Exam> {
    /**
     * 分页查询考试安排列表（支持按开课、教师等条件筛选）。
     *
     * @param params   查询条件参数
     * @param current  当前页码
     * @param size     每页条数
     * @return 分页结果
     */
    PageResult<Exam> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 查询教师被安排的考试ID列表。
     *
     * @param teacherId 教师ID
     * @return 考试ID列表
     */
    List<Long> getExamIdsByTeacherId(Long teacherId);

    /**
     * 查询学生的考试安排（含课程名称）。
     *
     * @param studentId 学生ID
     * @return 考试安排列表
     */
    List<Map<String, Object>> getMyExamSchedule(Long studentId);
}
