package com.ais.basic.service;

import com.ais.basic.entity.Major;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 专业服务接口。
 *
 * <p>提供专业的分页查询和全部列表查询功能。
 */
public interface MajorService extends IService<Major> {
    /**
     * 分页查询专业列表（支持按名称、代码、学院筛选）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<Major> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 查询所有正常状态的专业列表（用于下拉选择）。
     *
     * @return 专业列表
     */
    List<Major> listAll();
}
