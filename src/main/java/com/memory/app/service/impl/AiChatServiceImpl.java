package com.memory.app.service.impl;

import com.memory.app.service.AiChatService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI对话服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {
    
    private final ChatModel chatLanguageModel;

    @Override
    public String chat(String message, String context, Long knowledgeId) {
        log.info("AI聊天请求 - 知识点ID: {}, 消息: {}", knowledgeId, message);
        
        try {
            // 构建系统提示词
            String systemPrompt = "你是一个专业的学习助手，专门帮助用户理解和掌握知识点。\n" +
                    "请基于提供的知识点内容，为用户提供准确、有用的回答。\n" +
                    "你可以帮助用户：\n" +
                    "1. 总结和解释知识点\n" +
                    "2. 生成相关的面试题或练习题\n" +
                    "3. 制定学习计划\n" +
                    "4. 解答疑问\n" +
                    "5. 提供实践建议\n\n" +
                    "当前知识点内容：\n" + context;
            
            // 构建完整的消息
            String fullMessage = systemPrompt + "\n\n用户问题：" + message;
            
            // 调用AI模型
            String response = chatLanguageModel.chat(UserMessage.from(fullMessage)).aiMessage().text();
            
            log.info("AI响应成功 - 知识点ID: {}", knowledgeId);
            return response;
            
        } catch (Exception e) {
            log.error("AI聊天失败 - 知识点ID: {}, 错误: {}", knowledgeId, e.getMessage(), e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

}