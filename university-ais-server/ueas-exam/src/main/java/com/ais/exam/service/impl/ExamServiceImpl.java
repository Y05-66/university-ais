package com.ais.exam.service.impl;

import com.ais.basic.mapper.StudentCourseMapper;
import com.ais.basic.entity.StudentCourse;
import com.ais.common.result.PageResult;
import com.ais.exam.entity.Exam;
import com.ais.exam.mapper.ExamMapper;
import com.ais.exam.service.ExamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试安排服务实现类。
 *
 * <p>提供考试安排的分页查询功能，支持 JOIN 查询获取关联的课程和教师信息。
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    private final StudentCourseMapper studentCourseMapper;

    @Override
    public PageResult<Exam> pageQuery(Map<String, Object> params, long current, long size) {
        Page<Exam> page = new Page<>(current, size);
        IPage<Exam> result = baseMapper.selectPageWithJoin(page, params);
        return PageResult.of(result.getRecords(), result.getTotal(), current, size);
    }

    @Override
    public List<Long> getExamIdsByTeacherId(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectExamIdsByTeacherId(teacherId);
    }

    @Override
    public List<Map<String, Object>> getMyExamSchedule(Long studentId) {
        // 查询学生选课的考试安排
        List<Long> courseOfferIds = studentCourseMapper.selectList(
            new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getStatus, 1)
                .eq(StudentCourse::getDeleted, 0)
        ).stream().map(StudentCourse::getCourseOfferId).collect(Collectors.toList());
        
        if (courseOfferIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("courseOfferIds", courseOfferIds);
        
        IPage<Exam> result = baseMapper.selectPageWithJoin(new Page<>(1, 100), params);
        
        return result.getRecords().stream().map(exam -> {
            Map<String, Object> item = new HashMap<>();
            item.put("courseName", exam.getCourseName() != null ? exam.getCourseName() : "");
            item.put("examDate", exam.getExamDate());
            item.put("startTime", exam.getStartTime());
            item.put("endTime", exam.getEndTime());
            item.put("classroom", exam.getClassroom());
            item.put("seatCount", exam.getSeatCount());
            return item;
        }).collect(Collectors.toList());
    }
}
