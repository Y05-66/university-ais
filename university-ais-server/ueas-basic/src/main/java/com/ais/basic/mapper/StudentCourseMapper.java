package com.ais.basic.mapper;

import com.ais.basic.entity.StudentCourse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 学生选课记录数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供选课记录的自定义查询，
 * 包含 JOIN 查询（课程、教师信息）及时间冲突检测、成绩更新等操作。
 */
@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

    /**
     * 查询学生的选课列表（含课程、教师信息），支持分页。
     */
    @Select("<script>" +
            "SELECT sc.id, sc.student_id, sc.course_offer_id, sc.semester_id, " +
            "sc.select_time, sc.status, sc.score, sc.grade_point, sc.score_status, " +
            "sc.create_time, sc.update_time, sc.deleted, " +
            "c.name AS courseName, c.course_no AS courseNo, c.credits AS credits, " +
            "co.schedule AS schedule, co.weeks AS weeks, co.classroom AS classroom, " +
            "t.name AS teacherName " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN teacher t ON co.teacher_id = t.id AND t.deleted = 0 " +
            "WHERE sc.student_id = #{studentId} AND sc.status = 1 AND sc.deleted = 0 " +
            "<if test='semesterId != null'>AND sc.semester_id = #{semesterId}</if> " +
            "ORDER BY sc.create_time DESC" +
            "</script>")
    Page<StudentCourse> selectByStudentIdPage(Page<StudentCourse> page,
                                              @Param("studentId") Long studentId,
                                              @Param("semesterId") Long semesterId);

    /**
     * 查询学生的选课列表（不分页），用于课表查询。
     */
    @Select("<script>" +
            "SELECT sc.id, sc.student_id, sc.course_offer_id, sc.semester_id, " +
            "sc.select_time, sc.status, sc.score, sc.grade_point, sc.score_status, " +
            "sc.create_time, sc.update_time, sc.deleted, " +
            "c.name AS courseName, c.course_no AS courseNo, c.credits AS credits, c.category AS category, " +
            "co.schedule AS schedule, co.weeks AS weeks, co.classroom AS classroom, " +
            "t.name AS teacherName " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN teacher t ON co.teacher_id = t.id AND t.deleted = 0 " +
            "WHERE sc.student_id = #{studentId} AND sc.status = 1 AND sc.deleted = 0 " +
            "<if test='semesterId != null'>AND sc.semester_id = #{semesterId}</if> " +
            "ORDER BY sc.create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectStudentCourseWithOffer(@Param("studentId") Long studentId,
                                                             @Param("semesterId") Long semesterId);

    /**
     * 统计学生的选课总数（用于分页 total）。
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "WHERE sc.student_id = #{studentId} AND sc.status = 1 AND sc.deleted = 0 " +
            "<if test='semesterId != null'>AND sc.semester_id = #{semesterId}</if>" +
            "</script>")
    long countByStudentId(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);

    /**
     * 统计学生是否已选某课程（仅统计状态为已选的记录）。
     *
     * <p>注意：必须同时检查 status=1，否则退课后的历史记录也会导致"已选该课程"误判。
     */
    @Select("SELECT COUNT(*) FROM student_course " +
            "WHERE student_id = #{studentId} AND course_offer_id = #{courseOfferId} AND status = 1 AND deleted = 0")
    Long countSelected(@Param("studentId") Long studentId, @Param("courseOfferId") Long courseOfferId);

    /**
     * 检测时间冲突。
     *
     * <p>仅检查 status=1（已选）的记录，并关联非删除的开课记录。
     */
    @Select("SELECT COUNT(*) > 0 FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "WHERE sc.student_id = #{studentId} AND sc.semester_id = #{semesterId} " +
            "AND sc.status = 1 AND sc.deleted = 0 " +
            "AND co.schedule LIKE CONCAT('%', #{schedule}, '%') " +
            "AND co.weeks LIKE CONCAT('%', #{weeks}, '%')")
    boolean checkTimeConflict(@Param("studentId") Long studentId,
                               @Param("semesterId") Long semesterId,
                               @Param("schedule") String schedule,
                               @Param("weeks") String weeks);

    /**
     * 退课操作。
     */
    @Update("UPDATE student_course SET status = 0 WHERE student_id = #{studentId} AND course_offer_id = #{courseOfferId} AND status = 1")
    int dropCourse(@Param("studentId") Long studentId, @Param("courseOfferId") Long courseOfferId);

    /**
     * 查询教师所授课程的学生成绩列表（分页）。
     * teacherId 为 null 或 0 时查询全部（管理员使用）。
     */
    @Select("<script>" +
            "SELECT sc.id, sc.student_id, sc.course_offer_id, sc.semester_id, " +
            "sc.select_time, sc.status, sc.score, sc.grade_point, sc.score_status, " +
            "sc.create_time, sc.update_time, sc.deleted, " +
            "c.name AS courseName, c.course_no AS courseNo, c.credits AS credits, " +
            "co.schedule AS schedule, co.weeks AS weeks, co.classroom AS classroom, " +
            "t.name AS teacherName, co.teacher_id AS teacherId " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN teacher t ON co.teacher_id = t.id AND t.deleted = 0 " +
            "WHERE sc.status = 1 AND sc.deleted = 0 " +
            "<if test='teacherId != null and teacherId != 0'>AND co.teacher_id = #{teacherId}</if> " +
            "<if test='semesterId != null'>AND sc.semester_id = #{semesterId}</if> " +
            "ORDER BY sc.create_time DESC" +
            "</script>")
    Page<StudentCourse> selectByTeacherPage(Page<StudentCourse> page,
                                             @Param("teacherId") Long teacherId,
                                             @Param("semesterId") Long semesterId);

    /**
     * 统计教师所授课程的学生成绩总数。
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "WHERE sc.status = 1 AND sc.deleted = 0 " +
            "<if test='teacherId != null and teacherId != 0'>AND co.teacher_id = #{teacherId}</if> " +
            "<if test='semesterId != null'>AND sc.semester_id = #{semesterId}</if>" +
            "</script>")
    long countByTeacher(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    /**
     * 查询选课记录详情。
     */
    @Select("<script>" +
            "SELECT sc.id, sc.student_id, sc.course_offer_id, sc.semester_id, " +
            "sc.select_time, sc.status, sc.score, sc.grade_point, sc.score_status, " +
            "sc.create_time, sc.update_time, sc.deleted, " +
            "c.name AS courseName, c.course_no AS courseNo, c.credits AS credits, " +
            "co.schedule AS schedule, co.weeks AS weeks, co.classroom AS classroom, " +
            "t.name AS teacherName " +
            "FROM student_course sc " +
            "INNER JOIN course_offer co ON sc.course_offer_id = co.id AND co.deleted = 0 " +
            "INNER JOIN course c ON co.course_id = c.id AND c.deleted = 0 " +
            "LEFT JOIN teacher t ON co.teacher_id = t.id AND t.deleted = 0 " +
            "WHERE sc.id = #{id} AND sc.deleted = 0" +
            "</script>")
    StudentCourse selectDetailById(@Param("id") Long id);

    /**
     * 更新成绩。
     */
    @Update("UPDATE student_course " +
            "SET score = #{score}, grade_point = #{gradePoint}, score_status = #{scoreStatus}, update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int updateScore(@Param("id") Long id,
                    @Param("score") java.math.BigDecimal score,
                    @Param("gradePoint") java.math.BigDecimal gradePoint,
                    @Param("scoreStatus") Integer scoreStatus);
}
