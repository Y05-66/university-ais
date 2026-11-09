package com.ais.basic.service.impl;

import com.ais.basic.entity.Teacher;
import com.ais.basic.mapper.TeacherMapper;
import com.ais.basic.service.TeacherService;
import com.ais.common.config.CacheHelper;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 教师服务实现类。
 *
 * <p>提供教师信息的分页查询、列表查询及增删改操作，支持按工号查重和 Redis 缓存刷新。
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private final CacheHelper cacheHelper;

    public TeacherServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    /** 获取全部数据列表，带缓存支持 */
    @Override
    public List<Teacher> listAll() {
        return cacheHelper.getOrComputeList("teacher:list",
                () -> list(new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getDeleted, 0)
                        .orderByAsc(Teacher::getId)),
                30);
    }

    @Override
    public PageResult<Teacher> pageQuery(Map<String, Object> params, long current, long size) {
        IPage<Teacher> page = baseMapper.selectPage(new Page<>(current, size), params);
        return PageResult.of(page);
    }

    /** 新增数据并刷新缓存 */
    @Override
    public boolean save(Teacher teacher) {
        if (teacher.getUserId() == null) {
            // userId=0 means no linked user; keep null since ALTER TABLE allows it
        }
        if (teacher.getGender() == null) {
            teacher.setGender(1);
        }
        if (teacher.getStatus() == null) {
            teacher.setStatus(1);
        }
        if (teacher.getCollegeId() == null) {
            return false;
        }
        // Check if teacher number already exists among non-deleted records
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getTeacherNo, teacher.getTeacherNo())
                .eq(Teacher::getDeleted, 0));
        if (count != null && count > 0) {
            throw new com.ais.common.exception.BizException("工号已存在");
        }
        boolean result = super.save(teacher);
        if (result) cacheHelper.invalidate("teacher:list");
        return result;
    }

    /** 更新数据并刷新缓存 */
    @Override
    public boolean updateById(Teacher teacher) {
        boolean result = super.updateById(teacher);
        if (result) cacheHelper.invalidate("teacher:list");
        return result;
    }

    /** 删除数据并刷新缓存 */
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("teacher:list");
        return result;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        return obj instanceof Number ? ((Number) obj).longValue() : Long.parseLong(obj.toString());
    }
}
