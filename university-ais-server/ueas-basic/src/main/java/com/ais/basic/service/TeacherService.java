package com.ais.basic.service;

import com.ais.basic.entity.Teacher;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 教师服务接口。
 *
 * <p>提供教师的分页查询和全部列表查询功能。
 */
public interface TeacherService extends IService<Teacher> {
    /**
     * 分页查询教师列表（支持按工号、姓名、学院筛选）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<Teacher> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 查询所有正常状态的教师列表（用于下拉选择）。
     *
     * @return 教师列表
     */
    List<Teacher> listAll();
}
