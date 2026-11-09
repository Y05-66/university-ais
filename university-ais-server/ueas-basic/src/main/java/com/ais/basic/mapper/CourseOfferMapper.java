package com.ais.basic.mapper;

import com.ais.basic.entity.CourseOffer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 开课数据访问对象。
 *
 * <p>继承 MyBatis-Plus BaseMapper，提供开课记录的自定义查询，
 * 包含 JOIN 查询（课程、教师信息）及 enrolled 数量增减操作。
 */
@Mapper
public interface CourseOfferMapper extends BaseMapper<CourseOffer> {

    /**
     * 分页查询开课列表，JOIN 查询课程名称与教师姓名。
     *
     * @param page   分页参数
     * @param params 查询条件
     * @return 分页结果
     */
    IPage<CourseOffer> selectPageWithJoin(Page<CourseOffer> page, @Param("params") Map<String, Object> params);

    /**
     * 按条件查询开课列表（不分页）。
     *
     * @param courseId   课程 ID（可选）
     * @param teacherId  教师 ID（可选）
     * @param semesterId 学期 ID（可选）
     * @return 开课列表
     */
    List<CourseOffer> selectByConditions(@Param("courseId") Long courseId,
                                         @Param("teacherId") Long teacherId,
                                         @Param("semesterId") Long semesterId);

    /**
     * 增加指定开课的已选人数（乐观锁，版本不匹配时返回0表示并发冲突）。
     *
     * @param id 开课 ID
     * @param version 当前版本号
     * @return 更新行数，0 表示并发冲突或课程已满
     */
    int incrementEnrolled(@Param("id") Long id, @Param("version") Integer version);

    /**
     * 减少指定开课的已选人数（乐观锁）。
     *
     * @param id 开课 ID
     * @param version 当前版本号
     * @return 更新行数，0 表示并发冲突
     */
    int decrementEnrolled(@Param("id") Long id, @Param("version") Integer version);

    /**
     * 分页查询指定教师的授课列表。
     *
     * @param page     分页参数
     * @param teacherId 教师 ID
     * @return 分页结果
     */
    IPage<CourseOffer> selectPageByTeacherId(Page<CourseOffer> page, Long teacherId);
}
