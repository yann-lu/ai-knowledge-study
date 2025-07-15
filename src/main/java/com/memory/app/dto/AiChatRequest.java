package com.memory.app.dto;

import lombok.Data;

/**
 * AI对话请求DTO
 */
@Data
public class AiChatRequest {
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 知识点上下文（包含标题、大纲、内容等）
     */
    private String context;
    
    /**
     * 知识点ID
     */
    private Long knowledgeId;
}