package com.memory.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 复习策略配置类
 */
@Component
@ConfigurationProperties(prefix = "review.strategy")
@Data
public class ReviewStrategyConfig {
    
    /**
     * 未掌握时的处理策略
     * RESTART_FROM_TOMORROW: 从明天重新开始复习计划
     * ADD_EXTRA_REVIEWS: 增加额外复习次数，原计划顺延
     */
    private NotMasteredStrategy notMastered = NotMasteredStrategy.RESTART_FROM_TOMORROW;
    
    /**
     * 当使用ADD_EXTRA_REVIEWS策略时，额外增加的复习次数
     */
    private int extraReviewCount = 2;
    
    /**
     * 未掌握处理策略枚举
     */
    public enum NotMasteredStrategy {
        /**
         * 从明天重新开始复习计划（原策略）
         */
        RESTART_FROM_TOMORROW,
        
        /**
         * 增加额外复习次数，原计划顺延
         */
        ADD_EXTRA_REVIEWS
    }
}