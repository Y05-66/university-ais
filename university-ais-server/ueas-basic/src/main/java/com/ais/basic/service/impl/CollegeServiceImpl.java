package com.ais.basic.service.impl;

import com.ais.basic.entity.College;
import com.ais.basic.mapper.CollegeMapper;
import com.ais.basic.service.CollegeService;
import com.ais.common.config.CacheHelper;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * 学院服务实现类。
 *
 * <p>提供学院信息的分页查询、列表查询及增删改操作，支持 Redis 缓存刷新。
 */
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    private final CacheHelper cacheHelper;

    public CollegeServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    /** 获取全部数据列表，带缓存支持 */
    @Override
    public List<College> listAll() {
        return cacheHelper.getOrComputeList("college:list", () -> list(), 30);
    }

    @Override
    public PageResult<College> pageQuery(Map<String, Object> params, long current, long size) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<College> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        w.like(params.get("name") != null, College::getName, params.get("name"));
        w.like(params.get("code") != null, College::getCode, params.get("code"));
        w.orderByDesc(College::getId);
        IPage<College> page = baseMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(page);
    }

    /** 新增数据并刷新缓存 */
    @Override
    public boolean save(College college) {
        boolean result = super.save(college);
        if (result) cacheHelper.invalidate("college:list");
        return result;
    }

    /** 更新数据并刷新缓存 */
    @Override
    public boolean updateById(College college) {
        boolean result = super.updateById(college);
        if (result) cacheHelper.invalidate("college:list");
        return result;
    }

    /** 删除数据并刷新缓存 */
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("college:list");
        return result;
    }
}
