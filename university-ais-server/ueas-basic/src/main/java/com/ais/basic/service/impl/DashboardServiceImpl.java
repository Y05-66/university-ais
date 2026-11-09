package com.ais.basic.service.impl;

import com.ais.basic.entity.Course;
import com.ais.basic.entity.Student;
import com.ais.basic.entity.Teacher;
import com.ais.basic.mapper.CourseMapper;
import com.ais.basic.mapper.StudentMapper;
import com.ais.basic.mapper.TeacherMapper;
import com.ais.basic.mapper.StudentCourseMapper;
import com.ais.common.config.CacheHelper;
import com.ais.basic.service.DashboardService;
import com.ais.basic.dto.DashboardStats;
import com.ais.basic.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据看板服务实现类。
 *
 * <p>根据用户角色（管理员/教师/学生）返回不同的统计数据：
 * <ul>
 *   <li>管理员：全校统计数据</li>
 *   <li>教师：个人所授课程和成绩统计</li>
 *   <li>学生：个人选课和成绩统计</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final CourseMapper courseMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final CacheHelper cacheHelper;
    private final UserContextUtil userContextUtil;

    @Override
    public DashboardStats getDashboardStats(Integer userType) {
        log.info("getDashboardStats called with userType={}", userType);
        // 未登录时默认返回管理员统计数据
        int effectiveUserType = (userType != null) ? userType : 3;
        log.info("effectiveUserType={}", effectiveUserType);

        String cacheKey = "dashboard:stats:" + effectiveUserType;
        Long userId = userContextUtil.getCurrentUserId();
        log.info("cacheKey={}, userId={}", cacheKey, userId);

        return cacheHelper.getOrCompute(cacheKey, DashboardStats.class, () -> {
            DashboardStats stats = new DashboardStats();

            switch (effectiveUserType) {
                case 3 -> buildAdminStats(stats);       // 管理员
                case 2 -> buildTeacherStats(stats, userId);  // 教师
                case 1 -> buildStudentStats(stats, userId);  // 学生
                default -> buildAdminStats(stats);
            }

            return stats;
        }, 30);
    }

    /**
     * 构建管理员看板数据（全校统计）。
     */
    private void buildAdminStats(DashboardStats stats) {
        long studentCount = studentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                        .eq(Student::getDeleted, 0));
        log.info("Admin dashboard: studentCount={}", studentCount);
        stats.setStudentCount(studentCount);

        long teacherCount = teacherMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getDeleted, 0));
        log.info("Admin dashboard: teacherCount={}", teacherCount);
        stats.setTeacherCount(teacherCount);

        long courseCount = courseMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getDeleted, 0));
        log.info("Admin dashboard: courseCount={}", courseCount);
        stats.setCourseCount(courseCount);

        stats.setTotalEnrollment(courseMapper.selectTotalEnrolled());
        Double avgScore = courseMapper.selectAverageScore();
        stats.setAvgScore(avgScore != null ? avgScore.doubleValue() : 0.0);
        String passRate = courseMapper.selectPassRate();
        stats.setPassRate(passRate != null ? passRate : "0%");
        stats.setActiveSemester(courseMapper.getActiveSemesterName());
        stats.setPopularCourses(courseMapper.selectTopCourses(5));
        stats.setScoreDistribution(courseMapper.selectScoreDistribution());
        stats.setSemesterScoreTrend(courseMapper.selectSemesterScoreTrend());

        // 课程完成率（复用上面的 total 变量）
        Long completed = courseMapper.selectCompletedCourses();
        Long totalEnrollment = stats.getTotalEnrollment();
        stats.setCourseCompletionRate(String.format("%.1f%%",
                completed != null && totalEnrollment != null && totalEnrollment > 0
                        ? completed * 100.0 / totalEnrollment : 0.0));
    }

    /**
     * 构建教师看板数据（个人教学统计）。
     */
    private void buildTeacherStats(DashboardStats stats, Long userId) {
        List<Long> teacherIds = userContextUtil.getTeacherIds();
        if (teacherIds == null || teacherIds.isEmpty()) {
            return;
        }
        Long teacherId = teacherIds.get(0);

        stats.setTeacherStudentCount(studentCourseMapper.countByTeacher(teacherId, null));

        Double avgScore = courseMapper.selectTeacherAvgScore(teacherId);
        stats.setTeacherAvgScore(avgScore != null ? avgScore.doubleValue() : 0.0);
        stats.setTeacherPassRate(courseMapper.selectTeacherPassRate(teacherId));
        stats.setTeacherCourseCount(courseMapper.selectTeacherCourseCount(teacherId));

        stats.setTeacherPopularCourses(courseMapper.selectTeacherTopCourses(teacherId));
    }

    /**
     * 构建学生看板数据（个人学习统计）。
     */
    private void buildStudentStats(DashboardStats stats, Long userId) {
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return;
        }

        stats.setStudentCourseCount(studentCourseMapper.countByStudentId(studentId, null));

        Double avgScore = courseMapper.selectStudentAvgScore(studentId);
        stats.setStudentAvgScore(avgScore != null ? String.format("%.2f", avgScore) : "0.00");
        stats.setStudentPassRate(courseMapper.selectStudentPassRate(studentId));

        stats.setStudentScoreTrend(courseMapper.selectStudentScoreTrend(studentId));

        stats.setStudentUpcomingCourses(courseMapper.selectStudentCourses(studentId, null));
    }
}
