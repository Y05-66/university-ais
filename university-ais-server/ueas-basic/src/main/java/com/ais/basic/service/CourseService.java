package com.ais.basic.service;

import com.ais.basic.entity.Course;
import com.ais.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 课程服务接口。
 *
 * <p>提供课程的分页查询和全部列表查询功能。
 */
public interface CourseService extends IService<Course> {
    /**
     * 分页查询课程列表（支持按课程编号、名称、学院、类别筛选）。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<Course> pageQuery(Map<String, Object> params, long current, long size);

    /**
     * 查询所有正常状态的课程列表（用于下拉选择）。
     *
     * @return 课程列表
     */
    List<Course> listAll();
}
