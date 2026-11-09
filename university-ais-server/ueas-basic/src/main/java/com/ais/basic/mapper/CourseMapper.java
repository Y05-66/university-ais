package com.ais.basic.mapper;

import com.ais.basic.entity.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 课程数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供课程表的自定义分页查询，支持按课程编号、名称、学院、类别筛选。
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表，支持多条件筛选。
     *
     * @param page   分页参数
     * @param params 查询条件（courseNo、name、collegeId、category）
     * @return 分页结果
     */
    IPage<Course> selectPage(Page<Course> page, @Param("params") Map<String, Object> params);

    // ==================== 数据统计查询 ====================

    /**
     * 统计总选课人数。
     */
    @Select("SELECT COUNT(*) FROM student_course WHERE deleted = 0 AND status = 1")
    Long selectTotalEnrolled();

    /**
     * 查询平均成绩。
     */
    @Select("SELECT ROUND(AVG(score), 2) FROM student_course WHERE deleted = 0 AND score IS NOT NULL")
    Double selectAverageScore();

    /**
     * 查询及格率（成绩 >= 60）。
     */
    @Select("SELECT CONCAT(ROUND(COUNT(CASE WHEN score >= 60 THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 1), '%') " +
            "FROM student_course WHERE deleted = 0 AND score IS NOT NULL")
    String selectPassRate();

    /**
     * 查询当前活跃学期名称。
     */
    @Select("SELECT name FROM semester WHERE deleted = 0 AND status = 1 ORDER BY create_time DESC LIMIT 1")
    List<String> selectActiveSemesterNames();

    /**
     * 获取当前活跃学期名称（安全版）。
     */
    default String getActiveSemesterName() {
        List<String> names = selectActiveSemesterNames();
        return names != null && !names.isEmpty() ? names.get(0) : null;
    }

    /**
     * 统计已完成课程数（所有学生都录入了成绩）。
     */
    @Select("SELECT COUNT(*) as cnt FROM course_offer co " +
            "INNER JOIN student_course sc ON sc.course_offer_id = co.id AND sc.deleted = 0 AND sc.status = 1 " +
            "WHERE co.deleted = 0 AND sc.score_status = 1")
    Long selectCompletedCourses();

    /**
     * 查询热门课程 TOP N（按选课人数）。
     */
    @Select("SELECT c.name AS courseName, c.course_no AS courseNo, COUNT(sc.id) AS enrollmentCount " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 " +
            "GROUP BY co.id ORDER BY enrollmentCount DESC LIMIT #{limit}")
    List<Map<String, Object>> selectTopCourses(@Param("limit") int limit);

    /**
     * 查询成绩分布（按分数段统计）。
     */
    @Select("SELECT " +
            "SUM(CASE WHEN score >= 90 THEN 1 ELSE 0 END) AS a_count, " +
            "SUM(CASE WHEN score >= 80 AND score < 90 THEN 1 ELSE 0 END) AS b_count, " +
            "SUM(CASE WHEN score >= 70 AND score < 80 THEN 1 ELSE 0 END) AS c_count, " +
            "SUM(CASE WHEN score >= 60 AND score < 70 THEN 1 ELSE 0 END) AS d_count, " +
            "SUM(CASE WHEN score < 60 THEN 1 ELSE 0 END) AS f_count " +
            "FROM student_course WHERE deleted = 0 AND score IS NOT NULL")
    Map<String, Object> selectScoreDistribution();

    /**
     * 查询学期成绩趋势（最近5个学期平均分）。
     */
    @Select("SELECT s.name AS semesterName, ROUND(AVG(sc.score), 2) AS avgScore " +
            "FROM student_course sc " +
            "INNER JOIN semester s ON sc.semester_id = s.id AND s.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 AND sc.score IS NOT NULL " +
            "GROUP BY sc.semester_id ORDER BY s.create_time DESC LIMIT 5")
    List<Map<String, Object>> selectSemesterScoreTrend();

    // ==================== 角色化统计查询 ====================

    /**
     * 查询教师所授课程的平均成绩。
     */
    @Select("SELECT ROUND(AVG(sc.score), 2) FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 AND sc.score IS NOT NULL " +
            "AND co.teacher_id = #{teacherId}")
    Double selectTeacherAvgScore(@Param("teacherId") Long teacherId);

    /**
     * 查询教师所授课程的及格率。
     */
    @Select("SELECT CONCAT(ROUND(COUNT(CASE WHEN score >= 60 THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 1), '%') " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 AND sc.score IS NOT NULL " +
            "AND co.teacher_id = #{teacherId}")
    String selectTeacherPassRate(@Param("teacherId") Long teacherId);

    /**
     * 查询教师所授热门课程 TOP5。
     */
    @Select("SELECT c.name AS courseName, c.course_no AS courseNo, COUNT(sc.id) AS enrollmentCount " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 AND co.teacher_id = #{teacherId} " +
            "GROUP BY co.id ORDER BY enrollmentCount DESC LIMIT 5")
    List<Map<String, Object>> selectTeacherTopCourses(@Param("teacherId") Long teacherId);

    /**
     * 查询学生的平均成绩。
     */
    @Select("SELECT ROUND(AVG(score), 2) FROM student_course " +
            "WHERE deleted = 0 AND status = 1 AND score IS NOT NULL AND student_id = #{studentId}")
    Double selectStudentAvgScore(@Param("studentId") Long studentId);

    /**
     * 查询学生的及格率。
     */
    @Select("SELECT CONCAT(ROUND(COUNT(CASE WHEN score >= 60 THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 1), '%') " +
            "FROM student_course WHERE deleted = 0 AND status = 1 AND score IS NOT NULL AND student_id = #{studentId}")
    String selectStudentPassRate(@Param("studentId") Long studentId);

    /**
     * 查询学生的学期成绩趋势。
     */
    @Select("SELECT s.name AS semesterName, ROUND(AVG(sc.score), 2) AS avgScore, " +
            "COUNT(*) AS courseCount FROM student_course sc " +
            "INNER JOIN semester s ON sc.semester_id = s.id AND s.deleted = 0 " +
            "WHERE sc.deleted = 0 AND sc.status = 1 AND sc.student_id = #{studentId} " +
            "AND sc.score IS NOT NULL " +
            "GROUP BY sc.semester_id ORDER BY s.create_time DESC LIMIT 5")
    List<Map<String, Object>> selectStudentScoreTrend(@Param("studentId") Long studentId);

    /**
     * 查询学生当前学期的选课列表（含课程信息）。
     */
    @Select("<script>" +
            "SELECT c.name AS courseName, c.course_no AS courseNo, c.credits AS credits, " +
            "c.category AS category, t.name AS teacherName, co.schedule AS schedule, co.weeks AS weeks " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN teacher t ON co.teacher_id = t.id AND t.deleted = 0 " +
            "WHERE sc.student_id = #{studentId} AND sc.status = 1 AND sc.deleted = 0 " +
            "<if test='semesterId != null'>AND sc.semester_id = #{semesterId}</if> " +
            "ORDER BY sc.select_time DESC" +
            "</script>")
    List<Map<String, Object>> selectStudentCourses(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);

    /**
     * 统计教师所授课程的开课数量（按开课记录去重）。
     */
    @Select("SELECT COUNT(DISTINCT co.id) FROM course_offer co " +
            "WHERE co.deleted = 0 AND co.teacher_id = #{teacherId}")
    long selectTeacherCourseCount(@Param("teacherId") Long teacherId);

    /**
     * 统计学生已选课程的开课数量。
     */
    @Select("SELECT COUNT(DISTINCT sc.course_offer_id) FROM student_course sc " +
            "WHERE sc.deleted = 0 AND sc.status = 1 AND sc.student_id = #{studentId}")
    long selectStudentCourseOfferCount(@Param("studentId") Long studentId);
}
