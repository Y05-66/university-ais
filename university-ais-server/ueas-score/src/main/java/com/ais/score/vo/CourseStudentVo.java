package com.ais.score.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩录入课程视图对象。
 *
 * <p>按课程分组展示学生成绩录入列表，每个课程包含选课学生列表。
 */
public class CourseStudentVo {
    /** 开课 ID */
    private Long offerId;
    /** 课程 ID */
    private Long courseId;
    /** 课程编号 */
    private String courseNo;
    /** 课程名称 */
    private String courseName;
    /** 学分 */
    private BigDecimal credits;
    /** 学期 ID */
    private Long semesterId;
    /** 授课教师 ID */
    private Long teacherId;
    /** 已选人数 */
    private Integer enrolled;
    /** 已录入成绩人数 */
    private Integer gradedCount;
    /** 学生列表 */
    private List<StudentScoreVo> students;

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseNo() { return courseNo; }
    public void setCourseNo(String courseNo) { this.courseNo = courseNo; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public BigDecimal getCredits() { return credits; }
    public void setCredits(BigDecimal credits) { this.credits = credits; }
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Integer getEnrolled() { return enrolled; }
    public void setEnrolled(Integer enrolled) { this.enrolled = enrolled; }
    public Integer getGradedCount() { return gradedCount; }
    public void setGradedCount(Integer gradedCount) { this.gradedCount = gradedCount; }
    public List<StudentScoreVo> getStudents() { return students; }
    public void setStudents(List<StudentScoreVo> students) { this.students = students; }

    /**
     * 学生成绩视图对象。
     */
    public static class StudentScoreVo {
        private Long id;
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long offerId;
        private BigDecimal score;
        private Integer scoreStatus;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Long getOfferId() { return offerId; }
        public void setOfferId(Long offerId) { this.offerId = offerId; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public Integer getScoreStatus() { return scoreStatus; }
        public void setScoreStatus(Integer scoreStatus) { this.scoreStatus = scoreStatus; }
    }
}
