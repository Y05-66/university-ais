package com.ais.basic.service.impl;

import com.ais.basic.entity.Major;
import com.ais.basic.mapper.MajorMapper;
import com.ais.basic.service.MajorService;
import com.ais.common.config.CacheHelper;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * 专业服务实现类。
 *
 * <p>提供专业的分页查询、列表查询及增删改操作，支持 Redis 缓存。
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private final CacheHelper cacheHelper;

    public MajorServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public List<Major> listAll() {
        return cacheHelper.getOrComputeList("major:list",
                () -> list(new LambdaQueryWrapper<Major>()
                        .eq(Major::getDeleted, 0).orderByAsc(Major::getId)),
                30);
    }

    @Override
    public PageResult<Major> pageQuery(Map<String, Object> params, long current, long size) {
        IPage<Major> page = baseMapper.selectPage(new Page<>(current, size), buildWrapper(params));
        return PageResult.of(page);
    }

    private LambdaQueryWrapper<Major> buildWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getDeleted, 0)
                .like(params.get("name") != null, Major::getName, params.get("name"))
                .eq(params.get("collegeId") != null, Major::getCollegeId, params.get("collegeId"))
                .eq(params.get("status") != null, Major::getStatus, params.get("status"))
                .orderByDesc(Major::getCreateTime);
        return wrapper;
    }

    @Override
    public boolean save(Major major) {
        boolean result = super.save(major);
        if (result) cacheHelper.invalidate("major:list");
        return result;
    }

    @Override
    public boolean updateById(Major major) {
        boolean result = super.updateById(major);
        if (result) cacheHelper.invalidate("major:list");
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) cacheHelper.invalidate("major:list");
        return result;
    }
}
