package com.memory.app.service;

/**
 * AI对话服务接口
 */
public interface AiChatService {
    
    /**
     * 处理AI对话
     * 
     * @param message 用户消息
     * @param context 知识点上下文
     * @param knowledgeId 知识点ID
     * @return AI回复
     */
    String chat(String message, String context, Long knowledgeId);
}