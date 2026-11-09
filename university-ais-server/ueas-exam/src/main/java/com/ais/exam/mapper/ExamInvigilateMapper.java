package com.ais.exam.mapper;

import com.ais.exam.entity.ExamInvigilate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 监考安排数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供监考安排表的基础 CRUD 操作。
 */
@Mapper
public interface ExamInvigilateMapper extends BaseMapper<ExamInvigilate> {
}
