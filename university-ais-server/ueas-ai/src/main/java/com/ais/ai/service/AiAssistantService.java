package com.ais.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * AI 智能助手服务。
 *
 * <p>通过 RestTemplate 直接调用 OpenAI 兼容接口，
 * 手动解析 JSON 响应，规避 Spring AI 客户端的反序列化问题。
 */
@Slf4j
@Service
public class AiAssistantService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v3}")
    private String model;

    @Autowired
    public AiAssistantService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** 限流：同一秒内最多发一次请求，避免耗尽配额 */
    private volatile long lastCallMs = 0L;
    private static final long RATE_LIMIT_MS = 2500L;

    /**
     * 通用智能问答。
     */
    public String chat(String question, String context) {
        String systemPrompt = """
            你是高校教务系统的智能助手，专门解答与教务系统和学习相关的问题。\
            可以回答的问题范围：选课、成绩、考试安排、课表查询、通知公告、个人信息、课程管理等。\
            要求：\
            1. 回答要简洁直接，不要自我介绍（如"我是..."），用户提问后直接给出答案；\
            2. 如果问题与教务系统或学习无关（如聊天、翻译、代码编写等），回复："这个问题跟我当前的工作不太相关哦，我只负责教务系统相关的问题~";\
            3. 使用中文回答，语气友好自然。""";
        String userMessage = buildUserMessage(question, context);
        return callApi(systemPrompt, userMessage);
    }

    /**
     * 学生智能问答。
     */
    public String chatForStudent(String question, String context) {
        String systemPrompt = """
            你是高校教务系统的学生端智能助手，帮助学生解答选课、成绩、考试等问题。\
            要求：\
            1. 回答要简洁直接，不要自我介绍（如"我是..."），直接给出答案即可；\
            2. 如果问题与教务系统或学习无关，回复："这个问题跟我当前的工作不太相关哦，我只负责教务系统相关的问题~";\
            3. 使用友好易懂的语言。""";
        String userMessage = buildUserMessage(question, context);
        return callApi(systemPrompt, userMessage);
    }

    /**
     * 教师智能问答。
     */
    public String chatForTeacher(String question, String context) {
        String systemPrompt = """
            你是高校教务系统的教师端智能助手，帮助教师进行教学管理相关问题咨询。\
            可以回答的问题范围：成绩录入、监考安排、我的课表、学生评价等。\
            要求：\
            1. 回答要简洁直接，不要自我介绍，直接给出答案即可；\
            2. 如果问题与教务系统或教学工作无关，回复："这个问题跟我当前的工作不太相关哦，我只负责教务系统相关的问题~";\
            3. 使用专业严谨的语言。""";
        String userMessage = buildUserMessage(question, context);
        return callApi(systemPrompt, userMessage);
    }

    /**
     * 管理员智能问答。
     */
    public String chatForAdmin(String question, String context) {
        String systemPrompt = """
            你是高校教务系统的管理员端智能助手，帮助管理员了解系统功能和使用方法。\
            可以回答的问题范围：用户管理、角色权限、菜单配置、数据统计、系统设置等。\
            要求：\
            1. 回答要简洁直接，不要自我介绍，直接给出答案即可；\
            2. 如果问题与教务系统或管理工作无关，回复："这个问题跟我当前的工作不太相关哦，我只负责教务系统相关的问题~";\
            3. 使用数据驱动的表述方式。""";
        String userMessage = buildUserMessage(question, context);
        return callApi(systemPrompt, userMessage);
    }

    private String buildUserMessage(String question, String context) {
        if (context != null && !context.isBlank()) {
            return question + "\n\n相关数据：\n" + context;
        }
        return question;
    }

    /**
     * 统一调用 AI API。
     */
    private String callApi(String systemPrompt, String userMessage) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);

            // messages 数组
            List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            );
            requestBody.put("messages", messages);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String apiUrl = baseUrl.endsWith("/")
                ? baseUrl + "v1/chat/completions"
                : baseUrl + "/v1/chat/completions";

            log.info("Calling AI API: {} with model: {}", apiUrl, model);

            long now = System.currentTimeMillis();
            synchronized(this) {
                if (now - lastCallMs < RATE_LIMIT_MS) {
                    log.warn("AI rate limited (last call {}ms ago), returning cached reply");
                    return "请稍后再试，AI 接口当前限流中。";
                }
                lastCallMs = now;
            }

            String response = restTemplate.postForObject(apiUrl, request, String.class);

            // 解析响应
            if (response != null && !response.isBlank()) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        String content = message.has("content") ? message.get("content").asText() : "";
                        log.info("AI response received, length: {}", content.length());
                        return content;
                    }
                }
                // 检查是否有错误信息
                JsonNode error = root.get("error");
                if (error != null) {
                    String errorMsg = error.has("message") ? error.get("message").asText() : error.toString();
                    log.error("AI API returned error: {}", errorMsg);
                    return "AI 服务返回错误：" + errorMsg;
                }
            }
            log.warn("AI response is null or empty");
            return "抱歉，AI 服务返回了空响应，请稍后再试。";
        } catch (Exception e) {
            log.error("AI chat error: {}", e.getMessage(), e);
            return "抱歉，AI 服务暂时不可用，请稍后再试。";
        }
    }
}
