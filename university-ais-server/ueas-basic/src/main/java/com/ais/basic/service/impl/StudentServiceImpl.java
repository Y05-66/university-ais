package com.ais.basic.service.impl;

import com.ais.basic.entity.Student;
import com.ais.basic.mapper.StudentMapper;
import com.ais.basic.service.StudentService;
import com.ais.common.config.CacheHelper;
import com.ais.common.exception.BizException;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;

/**
 * 学生服务实现类。
 *
 * <p>提供学生的分页查询、详情获取、新增、修改和删除功能。
 * 使用 {@link CacheHelper} 在写操作后失效学生列表缓存。
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final CacheHelper cacheHelper;

    public StudentServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public PageResult<Student> pageQuery(Map<String, Object> params, long current, long size) {
        IPage<Student> page = baseMapper.selectPage(new Page<>(current, size), params);
        return PageResult.of(page);
    }

    @Override
    public Student getDetailById(Long id) {
        return baseMapper.selectDetailById(id);
    }

    @Override
    public boolean save(Student student) {
        if (student.getUserId() == null) {
            // userId=0 means no linked user; keep null since ALTER TABLE allows it
        }
        if (student.getGender() == null) {
            student.setGender(1);
        }
        if (student.getStatus() == null) {
            student.setStatus(1);
        }
        if (student.getCollegeId() == null) {
            return false;
        }
        if (student.getEnrollDate() == null) {
            student.setEnrollDate(java.time.LocalDate.now());
        }
        // Check if student number already exists among non-deleted records
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, student.getStudentNo())
                .eq(Student::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BizException("学号已存在");
        }
        boolean result = super.save(student);
        if (result) cacheHelper.invalidate("student:list");
        return result;
    }

    @Override
    public boolean updateById(Student student) {
        boolean result = super.updateById(student);
        if (result) cacheHelper.invalidate("student:list");
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("student:list");
        return result;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        return obj instanceof Number ? ((Number) obj).longValue() : Long.parseLong(obj.toString());
    }
}
