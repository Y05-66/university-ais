package com.ais.basic.mapper;

import com.ais.basic.entity.College;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学院数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供学院表的基础 CRUD 操作。
 */
@Mapper
public interface CollegeMapper extends BaseMapper<College> {
}
