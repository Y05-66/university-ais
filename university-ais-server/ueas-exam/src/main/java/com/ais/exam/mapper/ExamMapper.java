package com.ais.exam.mapper;

import com.ais.common.result.PageResult;
import com.ais.exam.entity.Exam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 考试安排数据访问对象。
 *
 * <p>提供考试安排表的自定义查询，包含与开课、课程、教师的 JOIN 查询。
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    /**
     * 分页查询考试安排列表，JOIN 查询课程名称与教师姓名。
     *
     * @param page   分页参数
     * @param params 查询条件（semesterId、courseId 等）
     * @return 分页结果
     */
    IPage<Exam> selectPageWithJoin(Page<Exam> page, @Param("params") Map<String, Object> params);

    /**
     * 查询教师被安排监考的考试ID列表。
     *
     * @param teacherId 教师ID
     * @return 考试ID列表
     */
    List<Long> selectExamIdsByTeacherId(@Param("teacherId") Long teacherId);
}
