package com.memory.app.service;

import com.memory.app.model.Knowledge;
import com.memory.app.model.ReviewPlan;
import com.memory.app.model.dto.ReviewDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewPlanService {
    void generateReviewPlan(Knowledge knowledge);
    void handleNotMastered(Long knowledgeId);
    List<ReviewDTO> getTodayReviews();
    ReviewDTO updateReviewStatus(Long reviewId, boolean mastered);
    Optional<ReviewPlan> findById(Long id);
} 