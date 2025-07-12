package com.memory.app.service;

import com.memory.app.model.Knowledge;
import com.memory.app.model.ReviewPlan;
import com.memory.app.model.dto.ReviewDTO;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewPlanService {
    void generateReviewPlan(Knowledge knowledge);
    void handleNotMastered(Long knowledgeId);
    List<ReviewDTO> getTodayReviews();
    ReviewDTO updateReviewStatus(Long reviewId, boolean mastered);
    Optional<ReviewPlan> findById(Long id);
    void markAsCompleted(Long reviewId);
    List<ReviewDTO> getReviewPlansByKnowledgeId(Long knowledgeId);
    Map<String, Object> getCalendarData(YearMonth yearMonth);
}