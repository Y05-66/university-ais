package com.ais.basic.mapper;

import com.ais.basic.entity.Major;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供专业表的基础 CRUD 操作。
 */
@Mapper
public interface MajorMapper extends BaseMapper<Major> {
}
