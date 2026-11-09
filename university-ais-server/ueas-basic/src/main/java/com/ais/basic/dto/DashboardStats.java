package com.ais.basic.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 首页仪表盘统计数据 DTO。
 *
 * <p>根据用户角色返回不同的统计字段：
 * <ul>
 *   <li>管理员：studentCount, teacherCount, courseCount, totalEnrollment, avgScore, passRate 等</li>
 *   <li>教师：teacherCourseCount, teacherStudentCount, teacherAvgScore, teacherPassRate 等</li>
 *   <li>学生：studentCourseCount, studentCompletedCount, studentGpa, studentCredits, studentAvgScore 等</li>
 * </ul>
 */
@Data
public class DashboardStats {
    // ==================== 管理员字段 ====================
    private long studentCount;
    private long teacherCount;
    private long courseCount;
    private long noticeCount;
    private long totalEnrollment;
    private double avgScore;
    private String passRate;
    private String activeSemester;
    private String courseCompletionRate;
    private List<Map<String, Object>> popularCourses;
    private Map<String, Object> scoreDistribution;
    private List<Map<String, Object>> semesterScoreTrend;

    // ==================== 教师字段 ====================
    private long teacherCourseCount;
    private long teacherStudentCount;
    private double teacherAvgScore;
    private String teacherPassRate;
    private List<Map<String, Object>> teacherPopularCourses;

    // ==================== 学生字段 ====================
    private long studentCourseCount;
    private long studentCompletedCount;
    private double studentGpa;
    private java.math.BigDecimal studentCredits;
    private String studentAvgScore;
    private String studentPassRate;
    private List<Map<String, Object>> studentScoreTrend;
    private List<Map<String, Object>> studentUpcomingCourses;
}
