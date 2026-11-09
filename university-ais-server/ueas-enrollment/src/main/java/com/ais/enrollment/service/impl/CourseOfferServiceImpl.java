package com.ais.enrollment.service.impl;

import com.ais.basic.entity.CourseOffer;
import com.ais.basic.mapper.CourseOfferMapper;
import com.ais.basic.mapper.StudentCourseMapper;
import com.ais.enrollment.service.CourseOfferService;
import com.ais.common.config.CacheHelper;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * 开课服务实现类。
 *
 * <p>提供开课记录的分页查询、列表查询及增删改操作，支持按教师ID查询授课列表。
 * 支持 Redis 缓存刷新。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseOfferServiceImpl extends ServiceImpl<CourseOfferMapper, CourseOffer> implements CourseOfferService {

    private final CacheHelper cacheHelper;
    private final StudentCourseMapper studentCourseMapper;

    @Override
    public PageResult<CourseOffer> pageQuery(Map<String, Object> params, long current, long size) {
        Page<CourseOffer> page = new Page<>(current, size);
        IPage<CourseOffer> result = baseMapper.selectPageWithJoin(page, params);
        return PageResult.of(result.getRecords(), result.getTotal(), current, size);
    }

    /** 获取全部数据列表，带缓存支持 */
    @Override
    public List<CourseOffer> listAll() {
        return cacheHelper.getOrComputeList("course-offer:list", () -> list(), 30);
    }

    @Override
    public PageResult<CourseOffer> myTeachingCourses(Long teacherId, long current, long size) {
        Page<CourseOffer> page = new Page<>(current, size);
        IPage<CourseOffer> result = baseMapper.selectPageByTeacherId(page, teacherId);
        return PageResult.of(result);
    }

    /** 新增数据并刷新缓存 */
    @Override
    public boolean save(CourseOffer entity) {
        boolean result = super.save(entity);
        if (result) cacheHelper.invalidate("course-offer:list");
        return result;
    }

    /** 更新数据并刷新缓存 */
    @Override
    public boolean updateById(CourseOffer entity) {
        boolean result = super.updateById(entity);
        if (result) cacheHelper.invalidate("course-offer:list");
        return result;
    }

    /** 删除数据并刷新缓存 */
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("course-offer:list");
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTimetable(Long teacherId) {
        List<CourseOffer> offers = baseMapper.selectByConditions(null, teacherId, null);
        return buildTimetable(offers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTimetableByTeacherAndSemester(Long teacherId, Long semesterId) {
        List<CourseOffer> offers = baseMapper.selectByConditions(null, teacherId, semesterId);
        return buildTimetable(offers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTimetableByStudent(Long studentId, Long semesterId) {
        // 查询学生选课记录，关联开课信息
        List<Map<String, Object>> enrollments = studentCourseMapper.selectStudentCourseWithOffer(
                studentId, semesterId
        );
        log.info("getTimetableByStudent: studentId={}, semesterId={}, enrollments.size={}",
                studentId, semesterId, enrollments != null ? enrollments.size() : 0);
        return buildTimetableFromEnrollments(enrollments);
    }

    /**
     * 将开课列表解析为课表记录（按星期和节次展开）。
     */
    private List<Map<String, Object>> buildTimetable(List<CourseOffer> offers) {
        List<Map<String, Object>> timetable = new ArrayList<>();
        java.util.Map<String, Integer> dayOrder = new java.util.LinkedHashMap<>();
        dayOrder.put("周一", 0);
        dayOrder.put("周二", 1);
        dayOrder.put("周三", 2);
        dayOrder.put("周四", 3);
        dayOrder.put("周五", 4);
        dayOrder.put("周六", 5);

        for (CourseOffer offer : offers) {
            if (offer.getSchedule() == null || offer.getSchedule().isEmpty()) {
                continue;
            }
            String[] slots = offer.getSchedule().split(",");
            for (String slot : slots) {
                slot = slot.trim();
                int dayIdx = slot.indexOf("周");
                if (dayIdx < 0) continue;
                // 匹配"周一"到"周日"
                String dayName = slot.substring(0, dayIdx + 2); // 取"周X"两个字符
                String periodStr = slot.substring(dayIdx + 2).replaceAll("[节,，]", "").trim();
                if (periodStr.contains("-")) {
                    String[] parts = periodStr.split("-");
                    try {
                        int start = Integer.parseInt(parts[0].trim());
                        int end = Integer.parseInt(parts[1].trim());
                        for (int p = start; p <= end; p++) {
                            Map<String, Object> entry = new HashMap<>();
                            entry.put("dayIndex", dayOrder.getOrDefault(dayName, 0));
                            entry.put("dayName", dayName);
                            entry.put("period", p);
                            entry.put("courseName", offer.getCourseName());
                            entry.put("courseId", offer.getCourseId());
                            entry.put("classroom", offer.getClassroom());
                            entry.put("weeks", offer.getWeeks());
                            entry.put("semesterId", offer.getSemesterId());
                            entry.put("semesterName", offer.getSemesterName());
                            entry.put("category", offer.getCategory());
                            timetable.add(entry);
                        }
                    } catch (NumberFormatException e) {
                        // skip malformed period
                    }
                }
            }
        }
        timetable.sort((a, b) -> {
            int cmp = ((Integer) a.get("dayIndex")).compareTo((Integer) b.get("dayIndex"));
            if (cmp != 0) return cmp;
            return ((Integer) a.get("period")).compareTo((Integer) b.get("period"));
        });
        return timetable;
    }

    /**
     * 将学生选课记录解析为课表记录（按星期和节次展开）。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildTimetableFromEnrollments(List<Map<String, Object>> enrollments) {
        List<Map<String, Object>> timetable = new ArrayList<>();
        java.util.Map<String, Integer> dayOrder = new java.util.LinkedHashMap<>();
        dayOrder.put("周一", 0);
        dayOrder.put("周二", 1);
        dayOrder.put("周三", 2);
        dayOrder.put("周四", 3);
        dayOrder.put("周五", 4);
        dayOrder.put("周六", 5);

        log.debug("buildTimetableFromEnrollments: enrollments.size={}", enrollments != null ? enrollments.size() : 0);
        for (Map<String, Object> enrollment : enrollments) {
            String schedule = (String) enrollment.get("schedule");
            log.debug("Processing schedule: {}", schedule);
            if (schedule == null || schedule.isEmpty()) {
                continue;
            }
            String[] slots = schedule.split(",");
            for (String slot : slots) {
                slot = slot.trim();
                int dayIdx = slot.indexOf("周");
                if (dayIdx < 0) continue;
                // 匹配"周一"到"周日"
                String dayName = slot.substring(0, dayIdx + 2); // 取"周X"两个字符
                String periodStr = slot.substring(dayIdx + 2).replaceAll("[节,，]", "").trim();
                log.debug("Slot: {}, dayName: {}, periodStr: {}", slot, dayName, periodStr);
                if (periodStr.contains("-")) {
                    String[] parts = periodStr.split("-");
                    try {
                        int start = Integer.parseInt(parts[0].trim());
                        int end = Integer.parseInt(parts[1].trim());
                        for (int p = start; p <= end; p++) {
                            Map<String, Object> entry = new HashMap<>();
                            entry.put("dayIndex", dayOrder.getOrDefault(dayName, 0));
                            entry.put("dayName", dayName);
                            entry.put("period", p);
                            entry.put("courseName", enrollment.get("courseName"));
                            entry.put("classroom", enrollment.get("classroom"));
                            entry.put("weeks", enrollment.get("weeks"));
                            entry.put("semesterId", enrollment.get("semesterId"));
                            entry.put("category", enrollment.get("category"));
                            timetable.add(entry);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Malformed period: {}", periodStr);
                    }
                }
            }
        }
        log.debug("Timetable entries before sort: {}", timetable.size());
        timetable.sort((a, b) -> {
            int cmp = ((Integer) a.get("dayIndex")).compareTo((Integer) b.get("dayIndex"));
            if (cmp != 0) return cmp;
            return ((Integer) a.get("period")).compareTo((Integer) b.get("period"));
        });
        log.debug("Timetable entries after sort: {}", timetable.size());
        return timetable;
    }
}
