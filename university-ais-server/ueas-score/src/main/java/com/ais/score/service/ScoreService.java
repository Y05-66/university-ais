package com.ais.score.service;

import com.ais.score.vo.CourseStudentVo;
import com.ais.score.vo.StudentScoreInputVo;
import com.ais.basic.entity.StudentCourse;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 成绩服务接口。
 *
 * <p>提供学生成绩的录入、查询、审核等功能。
 */
public interface ScoreService extends IService<StudentCourse> {
    /**
     * 查询学生本人的成绩列表。
     *
     * @param studentId 学生 ID
     * @param params    查询条件（如学期 ID）
     * @param current   当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    PageResult<StudentCourse> getMyScores(Long studentId, Map<String, Object> params, long current, long size);

    /**
     * 查询教师所授课程的成绩列表（分页）。
     *
     * @param teacherId  教师 ID
     * @param semesterId 学期 ID（可选）
     * @param current    当前页码
     * @param size       每页条数
     * @return 分页结果
     */
    PageResult<StudentCourse> getTeacherScores(Long teacherId, Long semesterId, long current, long size);

    /**
     * 录入或更新学生成绩。
     *
     * @param studentCourse 学生选课记录（含成绩信息）
     */
    void inputScore(StudentCourse studentCourse);

    /**
     * 审核成绩（确认发布）。
     *
     * @param id 选课记录 ID
     */
    void approveScore(Long id);

    /**
     * 发布成绩通知（异步操作）。
     *
     * @param id 选课记录 ID
     */
    void publishScore(Long id);

    /**
     * 修改成绩（仅管理员/教务员可操作）。
     *
     * @param id           选课记录 ID
     * @param newScore     新成绩
     * @param newScoreStatus 新成绩状态
     */
    void changeScore(Long id, java.math.BigDecimal newScore, Integer newScoreStatus);

    /**
     * 查询待审核成绩列表。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<StudentCourse> getApproveList(Map<String, Object> params, long current, long size);

    /**
     * 查询成绩录入列表（按课程分组，管理员看所有课程学生，教师只看自己授课的课程学生）。
     *
     * @param teacherIds 当前用户的教师ID列表（管理员传 [0L]）
     * @param semesterId 学期 ID（可选）
     * @return 课程学生列表（含每个学生的成绩信息）
     */
    java.util.List<CourseStudentVo> getScoreInputList(java.util.List<Long> teacherIds, Long semesterId);

    /**
     * 查询学员成绩录入列表（学员中心视图）。
     *
     * <p>管理员查看所有学员及其选课成绩；任课教师仅查看所授课程学员的成绩。
     *
     * @param teacherIds 当前用户的教师ID列表（管理员传 [0L]）
     * @param semesterId 学期 ID（可选）
     * @return 按学员分组的选课成绩列表
     */
    java.util.List<StudentScoreInputVo> getStudentScoreInputList(java.util.List<Long> teacherIds, Long semesterId);
}
