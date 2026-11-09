package com.ais.score.mapper;

import com.ais.score.vo.CourseStudentVo;
import com.ais.score.vo.ScoreInputStudentVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 成绩数据访问对象（独立于 ueas-basic，避免循环依赖）。
 *
 * <p>提供成绩录入相关的复杂查询，包括：
 * <ul>
 *   <li>按教师查询其授课课程的学生列表（用于成绩录入）</li>
 *   <li>按开课ID查询选课学生列表</li>
 *   <li>按学员聚合查询其所有选课成绩（学员中心视图）</li>
 * </ul>
 */
@Mapper
public interface ScoreInputMapper extends BaseMapper<com.ais.basic.entity.StudentCourse> {

    /**
     * 查询成绩录入列表（按课程分组）。
     */
    @Select("<script>" +
            "SELECT co.id as offerId, co.course_id as courseId, c.course_no as courseNo, c.name as courseName, c.credits as credits, " +
            "co.semester_id as semesterId, co.teacher_id as teacherId, " +
            "COUNT(sc.id) as enrolled, SUM(CASE WHEN sc.score_status = 1 THEN 1 ELSE 0 END) as gradedCount " +
            "FROM course_offer co " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN student_course sc ON sc.course_offer_id = co.id AND sc.deleted = 0 AND sc.status = 1 " +
            "WHERE co.deleted = 0 " +
            "<if test='teacherIds != null and teacherIds.size() == 1 and teacherIds[0] == 0'>" +
            "AND co.teacher_id IS NOT NULL" +
            "</if>" +
            "<if test='teacherIds != null and teacherIds.size() > 1'>" +
            "AND co.teacher_id IN " +
            "<foreach item='tid' collection='teacherIds' open='(' separator=',' close=')'>" +
            "#{tid}" +
            "</foreach>" +
            "</if>" +
            "<if test='teacherIds != null and teacherIds.size() == 1 and teacherIds[0] != 0'>" +
            "AND co.teacher_id = #{teacherIds[0]}" +
            "</if>" +
            "<if test='semesterId != null'>" +
            "AND co.semester_id = #{semesterId}" +
            "</if> " +
            "GROUP BY co.id ORDER BY co.id ASC" +
            "</script>")
    List<CourseStudentVo> selectScoreInputList(@Param("teacherIds") java.util.List<Long> teacherIds,
                                                @Param("semesterId") Long semesterId);

    /**
     * 批量查询多个开课的学生列表（修复 N+1 问题，替代 selectStudentsByOffer）。
     *
     * @param offerIds 开课ID列表
     * @return 学生列表，含 offerId 用于聚合
     */
    @Select("SELECT sc.id, sc.student_id, s.student_no, s.name AS student_name, " +
            "sc.course_offer_id AS offerId, sc.score, sc.score_status " +
            "FROM student_course sc " +
            "JOIN student s ON sc.student_id = s.id AND s.deleted = 0 " +
            "WHERE sc.course_offer_id IN <foreach item='oid' collection='offerIds' open='(' separator=',' close=')'>#{oid}</foreach> " +
            "AND sc.deleted = 0 AND sc.status = 1 " +
            "ORDER BY s.name")
    List<CourseStudentVo.StudentScoreVo> selectStudentsByOfferBatch(@Param("offerIds") java.util.List<Long> offerIds);

    /**
     * 查询指定开课的学生列表（保留用于单课程查询）。
     */
    @Select("SELECT sc.id, sc.student_id, s.student_no, s.name AS student_name, sc.score, sc.score_status " +
            "FROM student_course sc " +
            "JOIN student s ON sc.student_id = s.id AND s.deleted = 0 " +
            "WHERE sc.course_offer_id = #{courseOfferId} AND sc.deleted = 0 AND sc.status = 1 " +
            "ORDER BY s.name")
    List<CourseStudentVo.StudentScoreVo> selectStudentsByOffer(@Param("courseOfferId") Long courseOfferId);

    /**
     * 按学员查询其选课及成绩（学员中心视图）。
     */
    @Select("<script>" +
            "SELECT sc.id, sc.student_id, s.student_no, s.name AS student_name, " +
            "sc.course_offer_id, co.id AS offerId, c.course_no, c.name AS course_name, c.credits, " +
            "t.name AS teacher_name, sc.score, sc.score_status, sc.score_modify_count " +
            "FROM student_course sc " +
            "JOIN student s ON sc.student_id = s.id AND s.deleted = 0 " +
            "JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN teacher t ON co.teacher_id = t.id AND t.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 " +
            "<if test='teacherIds != null and teacherIds.size() == 1 and teacherIds[0] == 0'>" +
            "AND co.teacher_id IS NOT NULL" +
            "</if>" +
            "<if test='teacherIds != null and teacherIds.size() > 1'>" +
            "AND co.teacher_id IN " +
            "<foreach item='tid' collection='teacherIds' open='(' separator=',' close=')'>" +
            "#{tid}" +
            "</foreach>" +
            "</if>" +
            "<if test='teacherIds != null and teacherIds.size() == 1 and teacherIds[0] != 0'>" +
            "AND co.teacher_id = #{teacherIds[0]}" +
            "</if>" +
            "<if test='semesterId != null'>" +
            "AND co.semester_id = #{semesterId}" +
            "</if> " +
            "ORDER BY sc.student_id, co.id ASC" +
            "</script>")
    List<ScoreInputStudentVo> selectStudentScoreInputList(@Param("teacherIds") java.util.List<Long> teacherIds,
                                                          @Param("semesterId") Long semesterId);
}
