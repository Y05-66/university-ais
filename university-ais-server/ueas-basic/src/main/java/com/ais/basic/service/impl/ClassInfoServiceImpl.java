package com.ais.basic.service.impl;

import com.ais.basic.entity.ClassInfo;
import com.ais.basic.mapper.ClassInfoMapper;
import com.ais.basic.service.ClassInfoService;
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
 * 班级服务实现类。
 *
 * <p>提供班级的分页查询、列表查询及增删改操作，支持 Redis 缓存。
 */
@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {

    private final CacheHelper cacheHelper;

    public ClassInfoServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public List<ClassInfo> listAll() {
        return cacheHelper.getOrComputeList("class:list", () -> list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getDeleted, 0).orderByAsc(ClassInfo::getId)), 30);
    }

    @Override
    public PageResult<ClassInfo> pageQuery(Map<String, Object> params, long current, long size) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        w.like(params.get("name") != null, ClassInfo::getName, params.get("name"));
        w.like(params.get("code") != null, ClassInfo::getCode, params.get("code"));
        w.eq(params.get("majorId") != null, ClassInfo::getMajorId, params.get("majorId"));
        w.eq(params.get("grade") != null, ClassInfo::getGrade, params.get("grade"));
        w.orderByDesc(ClassInfo::getId);
        IPage<ClassInfo> page = baseMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(page);
    }

    @Override
    public boolean save(ClassInfo classInfo) {
        boolean result = super.save(classInfo);
        if (result) cacheHelper.invalidate("class:list");
        return result;
    }

    @Override
    public boolean updateById(ClassInfo classInfo) {
        boolean result = super.updateById(classInfo);
        if (result) cacheHelper.invalidate("class:list");
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("class:list");
        return result;
    }
}
