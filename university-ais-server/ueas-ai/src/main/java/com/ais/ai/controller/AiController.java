package com.ais.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.ais.ai.service.AiAssistantService;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 智能助手接口。
 */
@Slf4j
@Tag(name = "AI 智能助手")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;

    @Operation(summary = "通用智能问答")
    @SaCheckLogin
    @PostMapping("/chat")
    public R<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String context = request.get("context");
        
        if (question == null || question.isBlank()) {
            return R.fail("问题不能为空");
        }
        
        String answer = aiAssistantService.chat(question, context);
        return R.ok(Map.of("answer", answer));
    }

    @Operation(summary = "学生智能问答")
    @SaCheckLogin
    @PostMapping("/chat/student")
    public R<Map<String, String>> chatForStudent(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String context = request.get("context");
        
        if (question == null || question.isBlank()) {
            return R.fail("问题不能为空");
        }
        
        String answer = aiAssistantService.chatForStudent(question, context);
        return R.ok(Map.of("answer", answer));
    }

    @Operation(summary = "教师智能问答")
    @SaCheckLogin
    @PostMapping("/chat/teacher")
    public R<Map<String, String>> chatForTeacher(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String context = request.get("context");
        
        if (question == null || question.isBlank()) {
            return R.fail("问题不能为空");
        }
        
        String answer = aiAssistantService.chatForTeacher(question, context);
        return R.ok(Map.of("answer", answer));
    }

    @Operation(summary = "管理员智能问答")
    @SaCheckLogin
    @PostMapping("/chat/admin")
    public R<Map<String, String>> chatForAdmin(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String context = request.get("context");
        
        if (question == null || question.isBlank()) {
            return R.fail("问题不能为空");
        }
        
        String answer = aiAssistantService.chatForAdmin(question, context);
        return R.ok(Map.of("answer", answer));
    }
}
