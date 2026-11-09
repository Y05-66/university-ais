package com.ais.evaluation.service.impl;

import com.ais.common.result.PageResult;
import com.ais.evaluation.entity.EvaluationQuestionnaire;
import com.ais.evaluation.entity.EvaluationResult;
import com.ais.evaluation.mapper.EvaluationQuestionnaireMapper;
import com.ais.evaluation.mapper.EvaluationResultMapper;
import com.ais.evaluation.service.EvaluationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 教学评价服务实现类。
 *
 * <p>提供评价问卷的分页查询和学生评价提交功能。
 */
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationQuestionnaireMapper, EvaluationQuestionnaire> implements EvaluationService {

    private final EvaluationResultMapper resultMapper;

    @Override
    public PageResult<EvaluationQuestionnaire> pageQuery(Map<String, Object> params, long current, long size) {
        IPage<EvaluationQuestionnaire> page = baseMapper.selectPage(new Page<>(current, size), buildWrapper(params));
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(Long questionnaireId, Long studentId, Integer score, String comment) {
        EvaluationResult result = new EvaluationResult();
        result.setQuestionnaireId(questionnaireId);
        result.setStudentId(studentId);
        result.setScore(score);
        result.setComment(comment);
        resultMapper.insert(result);
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationQuestionnaire> buildWrapper(Map<String, Object> params) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationQuestionnaire> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        w.eq(params.get("teacherId") != null, EvaluationQuestionnaire::getTeacherId, params.get("teacherId"));
        w.eq(params.get("courseOfferId") != null, EvaluationQuestionnaire::getCourseOfferId, params.get("courseOfferId"));
        w.orderByDesc(EvaluationQuestionnaire::getId);
        return w;
    }
}
