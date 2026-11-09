package com.ais.basic.service.impl;

import com.ais.basic.entity.Course;
import com.ais.basic.mapper.CourseMapper;
import com.ais.basic.service.CourseService;
import com.ais.common.config.CacheHelper;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * 课程服务实现类。
 *
 * <p>提供课程的分页查询、列表查询及增删改操作，支持 Redis 缓存。
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CacheHelper cacheHelper;

    public CourseServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    /** 获取全部数据列表，带缓存支持 */
    @Override
    public List<Course> listAll() {
        return cacheHelper.getOrComputeList("course:list",
                () -> list(new LambdaQueryWrapper<Course>()
                        .eq(Course::getDeleted, 0)
                        .orderByAsc(Course::getId)),
                30);
    }

    @Override
    public PageResult<Course> pageQuery(Map<String, Object> params, long current, long size) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getDeleted, 0)
                .like(StringUtils.hasText((String) params.get("courseNo")), Course::getCourseNo, params.get("courseNo"))
                .like(StringUtils.hasText((String) params.get("name")), Course::getName, params.get("name"))
                .eq(params.get("collegeId") != null, Course::getCollegeId, toLong(params.get("collegeId")))
                .eq(params.get("category") != null, Course::getCategory, toInt(params.get("category")))
                .orderByDesc(Course::getCreateTime);

        IPage<Course> page = baseMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    /** 新增数据并刷新缓存 */
    @Override
    public boolean save(Course course) {
        boolean result = super.save(course);
        if (result) cacheHelper.invalidate("course:list");
        return result;
    }

    /** 更新数据并刷新缓存 */
    @Override
    public boolean updateById(Course course) {
        boolean result = super.updateById(course);
        if (result) cacheHelper.invalidate("course:list");
        return result;
    }

    /** 删除数据并刷新缓存 */
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("course:list");
        return result;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        return obj instanceof Number ? ((Number) obj).longValue() : Long.parseLong(obj.toString());
    }

    private Integer toInt(Object obj) {
        if (obj == null) return null;
        return obj instanceof Number ? ((Number) obj).intValue() : Integer.parseInt(obj.toString());
    }
}
