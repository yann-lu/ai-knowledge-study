package com.memory.app.controller;

import com.memory.app.dto.AiChatRequest;
import com.memory.app.dto.AiChatResponse;
import com.memory.app.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI对话控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiAiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiAiController.class);
    
    private final AiChatService aiChatService;

    /**
     * AI对话接口
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        try {
            logger.info("收到AI对话请求 - 知识点ID: {}, 消息长度: {}", 
                       request.getKnowledgeId(), 
                       request.getMessage() != null ? request.getMessage().length() : 0);
            
            // 调用AI服务处理对话
            String response = aiChatService.chat(request.getMessage(), request.getContext(), request.getKnowledgeId());
            
            AiChatResponse chatResponse = new AiChatResponse();
            chatResponse.setResponse(response);
            chatResponse.setSuccess(true);
            
            logger.info("AI对话响应成功 - 响应长度: {}", response.length());
            return ResponseEntity.ok(chatResponse);
            
        } catch (Exception e) {
            logger.error("AI对话处理失败", e);
            
            AiChatResponse errorResponse = new AiChatResponse();
            errorResponse.setResponse("抱歉，AI服务暂时不可用，请稍后再试。");
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
}