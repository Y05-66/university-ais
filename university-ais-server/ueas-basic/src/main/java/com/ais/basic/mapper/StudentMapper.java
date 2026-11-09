package com.ais.basic.mapper;

import com.ais.basic.entity.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 学生数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供学生表的自定义分页查询（含学院、专业、班级 JOIN）及详情查询。
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 根据 sys_user.id 查询学生记录。
     *
     * @param userId 系统用户ID
     * @return 学生信息，不存在返回null
     */
    @Select("SELECT * FROM student WHERE user_id = #{userId} AND deleted = 0 ORDER BY id DESC LIMIT 1")
    Student selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询学生列表，JOIN 查询学院、专业、班级名称。
     *
     * @param page   分页参数
     * @param params 查询条件（studentNo、name、collegeId、grade）
     * @return 分页结果
     */
    IPage<Student> selectPage(Page<Student> page, @Param("params") Map<String, Object> params);

    /**
     * 根据 ID 查询学生详情，JOIN 查询学院、专业、班级名称。
     *
     * @param id 学生 ID
     * @return 学生详情
     */
    Student selectDetailById(Long id);
}
