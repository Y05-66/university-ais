package com.ais.notice.mapper;

import com.ais.notice.entity.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供公告表的基础 CRUD 操作。
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
