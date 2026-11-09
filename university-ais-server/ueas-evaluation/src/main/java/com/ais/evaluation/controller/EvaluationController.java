package com.ais.evaluation.controller;

import com.ais.basic.entity.Student;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import com.ais.evaluation.entity.EvaluationQuestionnaire;
import com.ais.evaluation.service.EvaluationService;
import com.ais.security.util.SaTokenUtil;
import com.ais.basic.util.UserContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 教学评价控制器。
 *
 * <p>提供教学评价问卷的管理接口（管理员）以及学生提交评价的接口。
 */
@Tag(name = "教学评价")
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final UserContextUtil userContextUtil;
    

    /**
     * 将 sys_user.id 转换为 student.id。
     */

    @Operation(summary = "分页查询问卷列表")
    @GetMapping("/questionnaire/page")
    public R<PageResult<EvaluationQuestionnaire>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(evaluationService.pageQuery(params, current, size));
    }

    @Operation(summary = "新增问卷")
    @PostMapping("/questionnaire")
    public R<Void> add(@RequestBody EvaluationQuestionnaire q) {
        if (q == null || q.getName() == null || q.getTeacherId() == null || q.getCourseOfferId() == null) {
            return R.fail("问卷名称、教师和课程不能为空");
        }
        evaluationService.save(q);
        return R.ok();
    }

    @Operation(summary = "提交评价")
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody Map<String, Object> body) {
        if (body.get("questionnaireId") == null || body.get("score") == null) {
            return R.fail("问卷ID和评分不能为空");
        }
        Long questionnaireId;
        Integer score;
        try {
            questionnaireId = Long.parseLong(body.get("questionnaireId").toString());
            score = Integer.parseInt(body.get("score").toString());
        } catch (NumberFormatException e) {
            return R.fail("问卷ID和评分必须为有效数字");
        }
        if (score < 1 || score > 5) {
            return R.fail("评分必须在1-5之间");
        }
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";
        Long studentId = userContextUtil.getStudentId();
        if (studentId == null) {
            return R.fail("未登录或不是学生");
        }
        evaluationService.submitEvaluation(questionnaireId, studentId, score, comment);
        return R.ok();
    }

    @Operation(summary = "删除问卷")
    @DeleteMapping("/questionnaire/{id}")
    public R<Void> delete(@PathVariable Long id) {
        evaluationService.removeById(id);
        return R.ok();
    }
}
