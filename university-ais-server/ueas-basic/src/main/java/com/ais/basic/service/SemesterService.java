package com.ais.basic.service;

import com.ais.basic.entity.Semester;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 学期服务接口。
 *
 * <p>提供学期列表查询功能。
 */
public interface SemesterService extends IService<Semester> {
    /**
     * 查询所有学期列表（用于下拉选择）。
     *
     * @return 学期列表
     */
    List<Semester> listAll();
}
