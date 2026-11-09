package com.ais.score.service.impl;

import com.ais.basic.entity.StudentCourse;
import com.ais.basic.mapper.StudentCourseMapper;
import com.ais.common.config.CacheHelper;
import com.ais.common.exception.BizException;
import com.ais.common.result.PageResult;
import com.ais.common.result.ResultCode;
import com.ais.score.service.ScoreService;
import com.ais.score.vo.CourseStudentVo;
import com.ais.score.vo.ScoreInputStudentVo;
import com.ais.score.vo.StudentScoreInputVo;
import com.ais.score.mapper.ScoreInputMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩服务实现类。
 *
 * <p>提供成绩的录入、审核、修改和查询功能。
 * 核心权限：教师只能操作自己课程的成绩，管理员可操作全部。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse> implements ScoreService {

    private final ScoreInputMapper scoreInputMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final CacheHelper cacheHelper;

    @Override
    public PageResult<StudentCourse> getMyScores(Long studentId, Map<String, Object> params, long current, long size) {
        Page<StudentCourse> page = new Page<>(current, size);
        Long semesterId = params.get("semesterId") != null
                ? ((Number) params.get("semesterId")).longValue() : null;
        Page<StudentCourse> iPage = studentCourseMapper.selectByStudentIdPage(page, studentId, semesterId);
        return PageResult.of(iPage);
    }

    @Override
    public PageResult<StudentCourse> getTeacherScores(Long teacherId, Long semesterId, long current, long size) {
        // 使用数据库分页
        Page<StudentCourse> page = new Page<>(current, size);
        Page<StudentCourse> iPage = baseMapper.selectByTeacherPage(page, teacherId, semesterId);
        return PageResult.of(iPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inputScore(StudentCourse studentCourse) {
        StudentCourse sc = getById(studentCourse.getId());
        if (sc == null) {
            throw new BizException("选课记录不存在");
        }
        if (sc.getScoreStatus() == null || sc.getScoreStatus() == 0) {
            sc.setScore(studentCourse.getScore());
            sc.setGradePoint(calculateGradePoint(studentCourse.getScore()));
            sc.setScoreStatus(1);
        } else {
            int modifyCount = sc.getScoreModifyCount() != null ? sc.getScoreModifyCount() : 0;
            if (modifyCount >= 3) {
                throw new BizException("成绩已录入，最多允许修改3次，已超过修改次数限制");
            }
            sc.setScoreModifyCount(modifyCount + 1);
            sc.setScore(studentCourse.getScore());
            sc.setGradePoint(calculateGradePoint(studentCourse.getScore()));
        }
        updateById(sc);
        // 清除成绩缓存
        cacheHelper.invalidate("score:my:" + sc.getStudentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveScore(Long id) {
        StudentCourse sc = getById(id);
        if (sc == null) {
            throw new BizException("记录不存在");
        }
        if (sc.getScoreStatus() == 1) {
            throw new BizException("成绩已录入，无需重复审核");
        }
        sc.setScoreStatus(1);
        updateById(sc);
        cacheHelper.invalidate("score:my:" + sc.getStudentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishScore(Long id) {
        StudentCourse sc = getById(id);
        if (sc == null) {
            throw new BizException("记录不存在");
        }
        if (sc.getScoreStatus() != 1) {
            throw new BizException("成绩尚未录入，无法发布");
        }
        sc.setScoreStatus(2);
        updateById(sc);
        cacheHelper.invalidate("score:my:" + sc.getStudentId());
        log.info("成绩已发布: 选课记录ID={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeScore(Long id, BigDecimal newScore, Integer newScoreStatus) {
        StudentCourse sc = getById(id);
        if (sc == null) {
            throw new BizException("记录不存在");
        }
        if (sc.getScoreStatus() != null && sc.getScoreStatus() == 1) {
            throw new BizException(ResultCode.SCORE_ALREADY_APPROVED);
        }
        sc.setScore(newScore);
        sc.setGradePoint(calculateGradePoint(newScore));
        sc.setScoreStatus(newScoreStatus);
        updateById(sc);
        cacheHelper.invalidate("score:my:" + sc.getStudentId());
    }

    @Override
    public PageResult<StudentCourse> getApproveList(Map<String, Object> params, long current, long size) {
        IPage<StudentCourse> page = baseMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(params.get("scoreStatus") != null, StudentCourse::getScoreStatus, params.get("scoreStatus"))
                        .orderByDesc(StudentCourse::getId)
        );
        return PageResult.of(page);
    }

    @Override
    public List<CourseStudentVo> getScoreInputList(List<Long> teacherIds, Long semesterId) {
        List<CourseStudentVo> result = scoreInputMapper.selectScoreInputList(teacherIds, semesterId);
        if (result.isEmpty()) {
            return result;
        }
        // 修复 N+1：批量查询所有课程的学生，而非逐个查询
        List<Long> offerIds = result.stream().map(CourseStudentVo::getOfferId).collect(Collectors.toList());
        List<CourseStudentVo.StudentScoreVo> allStudents = scoreInputMapper.selectStudentsByOfferBatch(offerIds);
        // 按 offerId 分组
        Map<Long, List<CourseStudentVo.StudentScoreVo>> studentMap = allStudents.stream()
                .collect(Collectors.groupingBy(CourseStudentVo.StudentScoreVo::getOfferId));
        for (CourseStudentVo course : result) {
            course.setStudents(studentMap.getOrDefault(course.getOfferId(), Collections.emptyList()));
        }
        return result;
    }

    @Override
    public List<StudentScoreInputVo> getStudentScoreInputList(List<Long> teacherIds, Long semesterId) {
        List<ScoreInputStudentVo> rows = scoreInputMapper.selectStudentScoreInputList(teacherIds, semesterId);
        Map<Long, StudentScoreInputVo> studentMap = new LinkedHashMap<>();
        for (ScoreInputStudentVo row : rows) {
            StudentScoreInputVo vo = studentMap.get(row.getStudentId());
            if (vo == null) {
                vo = new StudentScoreInputVo();
                vo.setStudentId(row.getStudentId());
                vo.setStudentNo(row.getStudentNo());
                vo.setStudentName(row.getStudentName());
                vo.setCourses(new ArrayList<>());
                studentMap.put(row.getStudentId(), vo);
            }
            StudentScoreInputVo.CourseScoreItem item = new StudentScoreInputVo.CourseScoreItem();
            item.setStudentCourseId(row.getId());
            item.setOfferId(row.getCourseOfferId());
            item.setCourseNo(row.getCourseNo());
            item.setCourseName(row.getCourseName());
            item.setCredits(row.getCredits());
            item.setTeacherName(row.getTeacherName());
            item.setScore(row.getScore());
            item.setScoreStatus(row.getScoreStatus());
            item.setScoreModifyCount(row.getScoreModifyCount());
            vo.getCourses().add(item);
        }
        return new ArrayList<>(studentMap.values());
    }

    private BigDecimal calculateGradePoint(BigDecimal score) {
        if (score == null) return null;
        double s = score.doubleValue();
        if (s >= 90) return BigDecimal.valueOf(4.0);
        if (s >= 85) return BigDecimal.valueOf(3.7);
        if (s >= 82) return BigDecimal.valueOf(3.3);
        if (s >= 78) return BigDecimal.valueOf(3.0);
        if (s >= 75) return BigDecimal.valueOf(2.7);
        if (s >= 72) return BigDecimal.valueOf(2.3);
        if (s >= 68) return BigDecimal.valueOf(2.0);
        if (s >= 64) return BigDecimal.valueOf(1.5);
        if (s >= 60) return BigDecimal.valueOf(1.0);
        return BigDecimal.ZERO;
    }
}
