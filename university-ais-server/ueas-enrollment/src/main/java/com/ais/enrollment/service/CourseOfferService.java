package com.ais.enrollment.service;

import com.ais.basic.entity.CourseOffer;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 开课服务接口。
 *
 * <p>提供开课列表的分页查询、教师授课列表查询和全部列表查询功能。
 */
public interface CourseOfferService extends IService<CourseOffer> {
    /**
     * 分页查询开课列表（支持按课程、教师、学期筛选）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<CourseOffer> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 查询指定教师的授课列表。
     *
     * @param teacherId 教师 ID
     * @param current   当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    PageResult<CourseOffer> myTeachingCourses(Long teacherId, long current, long size);

    /**
     * 查询所有开课列表（用于下拉选择）。
     *
     * @return 开课列表
     */
    List<CourseOffer> listAll();

    /**
     * 查询教师课表（按星期和节次展开）。
     *
     * <p>将每个开课的 schedule 字段（如"周一3-4节,周三1-2节"）解析为多条课表记录，
     * 每条记录包含星期、开始节次、结束节次、课程名称、教室等信息。
     *
     * @param teacherId 教师 ID
     * @return 课表记录列表
     */
    List<Map<String, Object>> getTimetable(Long teacherId);

    /**
     * 查询指定教师在指定学期的课表（按星期和节次展开）。
     *
     * @param teacherId    教师 ID
     * @param semesterId   学期 ID（可为 null）
     * @return 课表记录列表
     */
    List<Map<String, Object>> getTimetableByTeacherAndSemester(Long teacherId, Long semesterId);

    /**
     * 查询指定学生在指定学期的课表（按星期和节次展开）。
     *
     * <p>通过学生选课记录关联开课信息，解析为课表格式。
     *
     * @param studentId    学生 ID
     * @param semesterId   学期 ID（可为 null）
     * @return 课表记录列表
     */
    List<Map<String, Object>> getTimetableByStudent(Long studentId, Long semesterId);
}
