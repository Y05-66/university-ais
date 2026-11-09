package com.ais.evaluation.mapper;

import com.ais.evaluation.entity.EvaluationResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价结果数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供评价结果表的基础 CRUD 操作。
 */
@Mapper
public interface EvaluationResultMapper extends BaseMapper<EvaluationResult> {
}
