package com.ais.basic.mapper;

import com.ais.basic.entity.Teacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 教师数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供教师表的自定义分页查询，支持按工号、姓名、学院筛选。
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    /**
     * 分页查询教师列表，支持多条件筛选。
     *
     * @param page   分页参数
     * @param params 查询条件（teacherNo、name、collegeId）
     * @return 分页结果
     */
    IPage<Teacher> selectPage(Page<Teacher> page, @Param("params") Map<String, Object> params);

    /**
     * 根据 sys_user.id 查询教师记录。
     *
     * @param userId 系统用户ID
     * @return 教师信息，不存在返回null
     */
    @Select("SELECT * FROM teacher WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    Teacher selectByUserId(@Param("userId") Long userId);

    /**
     * 根据 sys_user.id 查询所有教师记录（一个用户可能对应多个教师角色）。
     *
     * @param userId 系统用户ID
     * @return 教师列表
     */
    @Select("SELECT id FROM teacher WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectTeacherIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有有效教师 ID（用于管理员全量查询）。
     *
     * @return 所有教师 ID 列表
     */
    @Select("SELECT id FROM teacher WHERE deleted = 0")
    List<Long> selectAllTeacherIds();
}
