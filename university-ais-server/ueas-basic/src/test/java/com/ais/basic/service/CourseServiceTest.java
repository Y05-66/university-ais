package com.ais.basic.service;

import com.ais.basic.entity.Course;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 课程实体 {@link Course} 的冒烟测试。
 */
class CourseServiceTest {

    @Test
    void courseEntity_basicFieldsSetAndGet() {
        Course course = new Course();
        course.setName("高等数学");
        course.setCourseNo("MATH101");
        course.setCredits(new BigDecimal("4"));
        course.setStatus(1);
        course.setMaxStudents(60);

        assertEquals("高等数学", course.getName());
        assertEquals("MATH101", course.getCourseNo());
        assertEquals(BigDecimal.valueOf(4), course.getCredits());
        assertEquals(Integer.valueOf(1), course.getStatus());
        assertEquals(Integer.valueOf(60), course.getMaxStudents());
    }

    @Test
    void courseEntity_settersWorkCorrectly() {
        Course course = new Course();
        course.setName("");
        course.setCategory(2);
        course.setTotalHours(48);

        assertEquals("", course.getName());
        assertEquals(Integer.valueOf(2), course.getCategory());
        assertEquals(Integer.valueOf(48), course.getTotalHours());
    }
}
