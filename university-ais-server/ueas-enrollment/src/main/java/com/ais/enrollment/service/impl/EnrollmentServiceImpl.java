package com.ais.enrollment.service.impl;

import com.ais.basic.entity.CourseOffer;
import com.ais.basic.entity.StudentCourse;
import com.ais.basic.mapper.CourseOfferMapper;
import com.ais.basic.mapper.StudentCourseMapper;
import com.ais.common.config.CacheHelper;
import com.ais.common.exception.BizException;
import com.ais.enrollment.service.EnrollmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 选课服务实现类。
 *
 * <p>处理学生选课、退课逻辑，支持时间冲突检测、乐观锁并发控制。
 * 使用 CacheHelper 维护课程列表缓存。
 */
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final StudentCourseMapper studentCourseMapper;
    private final CourseOfferMapper courseOfferMapper;
    private final CacheHelper cacheHelper;

    @Override
    public IPage<Map<String, Object>> getMyCourses(Long studentId, Long semesterId, long current, long size) {
        // 使用数据库分页，避免全量加载到内存
        Page<StudentCourse> page = new Page<>(current, size);
        Page<StudentCourse> iPage = studentCourseMapper.selectByStudentIdPage(page, studentId, semesterId);

        List<Map<String, Object>> result = iPage.getRecords().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Page<Map<String, Object>> resultPage = new Page<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal());
        resultPage.setRecords(result);
        return resultPage;
    }

    /**
     * 学生选课，使用乐观锁保证并发安全。
     *
     * <p>关键步骤：
     * <ol>
     *   <li>检查是否已选（status=1）</li>
     *   <li>检查时间冲突</li>
     *   <li>乐观锁增加已选人数（version 不匹配则说明有并发竞争）</li>
     *   <li>插入选课记录</li>
     * </ol>
     *
     * <p>数据库 UNIQUE KEY (student_id, course_offer_id) 作为最后一道防线，
     * 防止并发场景下插入重复记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectCourse(Long studentId, Long courseOfferId) {
        // 检查是否已选（status=1）
        Long count = studentCourseMapper.countSelected(studentId, courseOfferId);
        if (count != null && count > 0) {
            throw new BizException("已选该课程");
        }

        // 查询开课信息（含版本号）
        CourseOffer offer = courseOfferMapper.selectById(courseOfferId);
        if (offer == null) {
            throw new BizException("课程不存在");
        }
        // 安全检查：开课状态必须是正常（1），已取消的课程不能选课
        if (offer.getStatus() == null || offer.getStatus() != 1) {
            throw new BizException("该课程已取消，无法选课");
        }
        // 安全检查：开课容量不能为空
        if (offer.getMaxStudents() == null || offer.getMaxStudents() <= 0) {
            throw new BizException("该课程容量设置异常，请联系管理员");
        }

        // 检查时间冲突（SQL 中已包含 co.deleted=0 过滤）
        if (studentCourseMapper.checkTimeConflict(studentId, offer.getSemesterId(),
                offer.getSchedule(), offer.getWeeks())) {
            throw new BizException("时间冲突");
        }

        // 乐观锁更新：version 不匹配说明有并发竞争，抛出异常
        int updated = courseOfferMapper.incrementEnrolled(courseOfferId, offer.getVersion());
        if (updated == 0) {
            // 重新查询最新信息，返回更精确的错误提示
            CourseOffer latest = courseOfferMapper.selectById(courseOfferId);
            if (latest != null && latest.getEnrolled() != null && latest.getMaxStudents() != null
                    && latest.getEnrolled() >= latest.getMaxStudents()) {
                throw new BizException("课程已满，请选择其他课程");
            }
            throw new BizException("有人抢先选了这门课，请刷新后重试");
        }

        // 创建选课记录
        StudentCourse sc = new StudentCourse();
        sc.setStudentId(studentId);
        sc.setCourseOfferId(courseOfferId);
        sc.setSemesterId(offer.getSemesterId());
        sc.setStatus(1);
        sc.setSelectTime(LocalDateTime.now());
        try {
            studentCourseMapper.insert(sc);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 数据库唯一键 (student_id, course_offer_id) 触发，说明并发窗口内已被其他人抢先
            // 回滚乐观锁的 incrementEnrolled，通知用户重试
            throw new BizException("选课冲突，请稍后重试");
        }

        // 清除开课列表缓存
        cacheHelper.invalidate("course-offer:list");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dropCourse(Long studentId, Long courseOfferId) {
        studentCourseMapper.dropCourse(studentId, courseOfferId);

        // 查询开课信息获取版本号
        CourseOffer offer = courseOfferMapper.selectById(courseOfferId);
        if (offer != null) {
            courseOfferMapper.decrementEnrolled(courseOfferId, offer.getVersion());
        }

        // 清除开课列表缓存
        cacheHelper.invalidate("course-offer:list");
    }

    @Override
    public List<Map<String, Object>> getAvailableCourses(Long studentId, Long semesterId) {
        LambdaQueryWrapper<CourseOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOffer::getSemesterId, semesterId)
               .eq(CourseOffer::getStatus, 1)
               .eq(CourseOffer::getDeleted, 0);

        List<CourseOffer> offers = courseOfferMapper.selectList(wrapper);

        // 批量查询已选课程ID，避免 N+1 查询
        if (offers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> offerIds = offers.stream().map(CourseOffer::getId).collect(Collectors.toList());
        List<StudentCourse> myCourses = studentCourseMapper.selectList(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getStudentId, studentId)
                        .eq(StudentCourse::getDeleted, 0)
                        .in(StudentCourse::getCourseOfferId, offerIds)
        );
        // 只统计 status=1（已选）的课程ID，退课(status=0)的不视为已选
        Set<Long> selectedOfferIds = myCourses.stream()
                .filter(sc -> sc.getStatus() != null && sc.getStatus() == 1)
                .map(StudentCourse::getCourseOfferId)
                .collect(Collectors.toSet());

        return offers.stream()
                .filter(offer -> !selectedOfferIds.contains(offer.getId()))
                .filter(offer -> offer.getEnrolled() != null
                        && offer.getMaxStudents() != null
                        && offer.getEnrolled() < offer.getMaxStudents())
                .map(this::offerToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getEnrollmentRoster(Long courseOfferId) {
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getCourseOfferId, courseOfferId)
               .eq(StudentCourse::getStatus, 1)
               .eq(StudentCourse::getDeleted, 0)
               .orderByDesc(StudentCourse::getSelectTime);

        return studentCourseMapper.selectList(wrapper).stream()
                .map(sc -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("studentId", sc.getStudentId());
                    map.put("selectTime", sc.getSelectTime());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(StudentCourse sc) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", sc.getId());
        map.put("studentId", sc.getStudentId());
        map.put("courseOfferId", sc.getCourseOfferId());
        map.put("score", sc.getScore());
        map.put("scoreStatus", sc.getScoreStatus());
        map.put("selectTime", sc.getSelectTime());
        map.put("courseName", sc.getCourseName());
        map.put("courseNo", sc.getCourseNo());
        map.put("credits", sc.getCredits());
        map.put("teacherName", sc.getTeacherName());
        map.put("schedule", sc.getSchedule());
        map.put("weeks", sc.getWeeks());
        map.put("classroom", sc.getClassroom());
        return map;
    }

    private Map<String, Object> offerToMap(CourseOffer offer) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", offer.getId());
        map.put("courseId", offer.getCourseId());
        map.put("teacherId", offer.getTeacherId());
        map.put("semesterId", offer.getSemesterId());
        map.put("classroom", offer.getClassroom());
        map.put("schedule", offer.getSchedule());
        map.put("weeks", offer.getWeeks());
        map.put("maxStudents", offer.getMaxStudents());
        map.put("enrolled", offer.getEnrolled());
        map.put("status", offer.getStatus());
        return map;
    }
}
