package com.ais.evaluation.service;

import com.ais.common.result.PageResult;
import com.ais.evaluation.entity.EvaluationQuestionnaire;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 教学评价服务接口。
 *
 * <p>提供问卷的分页查询和学生提交评价的功能。
 */
public interface EvaluationService extends IService<EvaluationQuestionnaire> {
    /**
     * 分页查询评价问卷列表。
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<EvaluationQuestionnaire> pageQuery(Map<String, Object> params, long current, long size);
    /**
     * 提交教学评价。
     *
     * @param questionnaireId 问卷 ID
     * @param studentId       学生 ID
     * @param score           评分（1-5分）
     * @param comment         评价留言
     */
    void submitEvaluation(Long questionnaireId, Long studentId, Integer score, String comment);
}
