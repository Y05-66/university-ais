package com.ais.basic.mapper;

import com.ais.basic.entity.ClassInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级管理数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供班级表的基础 CRUD 操作。
 */
@Mapper
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {
}
