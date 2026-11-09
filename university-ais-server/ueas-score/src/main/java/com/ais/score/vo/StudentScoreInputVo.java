package com.ais.score.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生成绩录入视图对象（学生中心视图）。
 *
 * <p>以学员为主体，展示其选课及成绩信息。
 * 管理员查看全体学员，教师仅查看所授课程的学员。
 */
public class StudentScoreInputVo {

    /** 学生 ID */
    private Long studentId;
    /** 学号 */
    private String studentNo;
    /** 姓名 */
    private String studentName;
    /** 选课及成绩列表 */
    private List<CourseScoreItem> courses;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public List<CourseScoreItem> getCourses() { return courses; }
    public void setCourses(List<CourseScoreItem> courses) { this.courses = courses; }

    /**
     * 单门课程的成績条目。
     */
    public static class CourseScoreItem {
        /** 选课记录 ID（用于更新成绩） */
        private Long studentCourseId;
        /** 开课 ID */
        private Long offerId;
        /** 课程编号 */
        private String courseNo;
        /** 课程名称 */
        private String courseName;
        /** 学分 */
        private BigDecimal credits;
        /** 授课教师姓名 */
        private String teacherName;
        /** 成绩 */
        private BigDecimal score;
        /** 成绩状态：null=未录, 1=已录 */
        private Integer scoreStatus;
        /** 成绩修改次数 */
        private Integer scoreModifyCount;

        public Long getStudentCourseId() { return studentCourseId; }
        public void setStudentCourseId(Long studentCourseId) { this.studentCourseId = studentCourseId; }
        public Long getOfferId() { return offerId; }
        public void setOfferId(Long offerId) { this.offerId = offerId; }
        public String getCourseNo() { return courseNo; }
        public void setCourseNo(String courseNo) { this.courseNo = courseNo; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public BigDecimal getCredits() { return credits; }
        public void setCredits(BigDecimal credits) { this.credits = credits; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public Integer getScoreStatus() { return scoreStatus; }
        public void setScoreStatus(Integer scoreStatus) { this.scoreStatus = scoreStatus; }
        public Integer getScoreModifyCount() { return scoreModifyCount; }
        public void setScoreModifyCount(Integer scoreModifyCount) { this.scoreModifyCount = scoreModifyCount; }
    }
}
