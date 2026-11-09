package com.ais.system.mapper;

import com.ais.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问对象。
 *
 * <p>提供用户表的 CRUD 操作，以及分页查询和详情查询的自定义方法。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户列表（支持模糊搜索）。
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysUser> pageQuery(IPage<SysUser> page, SysUser query);

    /**
     * 根据用户 ID 查询用户详情（包含关联信息）。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    SysUser selectDetailById(Long id);

    /**
     * 查询所有正常状态的用户（用于下拉选择）。
     *
     * @return 用户列表
     */
    java.util.List<SysUser> selectAllActive();
}
