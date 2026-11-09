package com.ais.exam.mapper;

import com.ais.exam.entity.ExamRoom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考场数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供考场表的基础 CRUD 操作。
 */
@Mapper
public interface ExamRoomMapper extends BaseMapper<ExamRoom> {
}
