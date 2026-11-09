package com.ais.score.vo;

import java.math.BigDecimal;

/**
 * 成绩录入学生信息 VO。
 */
public class ScoreInputStudentVo {
    /** 选课记录 ID */
    private Long id;
    /** 学生 ID */
    private Long studentId;
    /** 学号 */
    private String studentNo;
    /** 学生姓名 */
    private String studentName;
    /** 开课 ID */
    private Long courseOfferId;
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
    /** 成绩状态：0=未录, 1=已录 */
    private Integer scoreStatus;
    /** 成绩修改次数（最多允许修改3次） */
    private Integer scoreModifyCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getCourseOfferId() { return courseOfferId; }
    public void setCourseOfferId(Long courseOfferId) { this.courseOfferId = courseOfferId; }
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
