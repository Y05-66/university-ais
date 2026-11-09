package com.ais.basic.service;

import com.ais.basic.entity.ClassInfo;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 班级服务接口。
 *
 * <p>提供班级的分页查询和全部列表查询功能。
 */
public interface ClassInfoService extends IService<ClassInfo> {
    /**
     * 分页查询班级列表（支持按名称、年级筛选）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<ClassInfo> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 查询所有正常状态的班级列表（用于下拉选择）。
     *
     * @return 班级列表
     */
    List<ClassInfo> listAll();
}
