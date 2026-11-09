package com.ais.basic.service.impl;

import com.ais.basic.entity.Semester;
import com.ais.basic.mapper.SemesterMapper;
import com.ais.basic.service.SemesterService;
import com.ais.common.config.CacheHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学期服务实现类。
 *
 * <p>提供学期列表查询功能，支持 Redis 缓存以提高查询性能。
 */
@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {

    private final CacheHelper cacheHelper;

    public SemesterServiceImpl(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public List<Semester> listAll() {
        return cacheHelper.getOrComputeList("semester:list",
                () -> list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Semester>()
                        .orderByDesc(Semester::getId)),
                30);
    }
}
