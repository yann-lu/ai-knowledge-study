package com.memory.app.dto;

import lombok.Data;

/**
 * AI对话响应DTO
 */
@Data
public class AiChatResponse {
    
    /**
     * AI回复内容
     */
    private String response;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果有）
     */
    private String error;
}