package com.ais.system.mapper;

import com.ais.system.entity.SysLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * 系统日志数据访问对象。
 *
 * <p>提供操作日志表的 CRUD 操作，以及按时间清理过期日志的方法。
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    /**
     * 分页查询操作日志列表。
     *
     * @param page   分页参数
     * @param params 查询条件（如用户名、操作类型等）
     * @return 分页结果
     */
    IPage<SysLog> selectPage(IPage<SysLog> page, @Param("params") Map<String, Object> params);

    /**
     * 软删除 30 天前的日志记录。
     *
     * @return 删除的记录数
     */
    /** 软删除30天前的日志 */
    @Update("UPDATE sys_log SET deleted = 1 WHERE deleted = 0 AND create_time < DATE_SUB(NOW(), INTERVAL 30 DAY)")
    int deleteLogByDays();
}
