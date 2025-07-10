package com.memory.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.memory.app.model.Knowledge;
import com.memory.app.model.ReviewPlan;
import com.memory.app.model.dto.ReviewDTO;
import com.memory.app.repository.ReviewPlanRepository;
import com.memory.app.service.ReviewPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewPlanServiceImpl implements ReviewPlanService {

    private final ReviewPlanRepository reviewPlanRepository;

    @Override
    @Transactional
    public void generateReviewPlan(Knowledge knowledge) {
        // 艾宾浩斯记忆曲线的复习间隔（天数）
        int[] reviewIntervals = {0, 1, 2, 4, 7, 14, 21, 30, 45, 60};
        
        // 删除所有现有的复习计划
        List<ReviewPlan> existingPlans = reviewPlanRepository.findByKnowledgeId(knowledge.getId());
        if (!existingPlans.isEmpty()) {
            reviewPlanRepository.deleteAll(existingPlans);
        }
        
        // 创建新的复习计划
        List<ReviewPlan> plans = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < reviewIntervals.length; i++) {
            ReviewPlan plan = new ReviewPlan();
            plan.setKnowledge(knowledge);
            plan.setReviewStage(i + 1);
            plan.setScheduledDate(today.plusDays(reviewIntervals[i]));
            plan.setStatus(ReviewPlan.ReviewStatus.PENDING);
            plans.add(plan);
        }
        
        reviewPlanRepository.saveAll(plans);
    }

    @Override
    @Transactional
    public void handleNotMastered(Long knowledgeId) {
        // 删除所有现有的复习计划
        List<ReviewPlan> existingPlans = reviewPlanRepository.findByKnowledgeId(knowledgeId);
        if (!existingPlans.isEmpty()) {
            reviewPlanRepository.deleteAll(existingPlans);
        }

        // 创建新的复习计划，从明天开始
        List<ReviewPlan> plans = new ArrayList<>();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // 第二次复习：明天
        ReviewPlan secondReview = new ReviewPlan();
        secondReview.setKnowledgeId(knowledgeId);
        secondReview.setReviewStage(2);
        secondReview.setScheduledDate(tomorrow);
        secondReview.setStatus(ReviewPlan.ReviewStatus.PENDING);
        plans.add(secondReview);

        // 从第三次复习开始，使用标准的艾宾浩斯间隔
        int[] reviewIntervals = {2, 4, 7, 14, 21, 30, 45, 60}; // 从第二次复习后的间隔
        for (int i = 0; i < reviewIntervals.length; i++) {
            ReviewPlan plan = new ReviewPlan();
            plan.setKnowledgeId(knowledgeId);
            plan.setReviewStage(i + 3); // 从第3阶段开始
            plan.setScheduledDate(tomorrow.plusDays(reviewIntervals[i]));
            plan.setStatus(ReviewPlan.ReviewStatus.PENDING);
            plans.add(plan);
        }

        reviewPlanRepository.saveAll(plans);
    }

    private static final Logger log = LoggerFactory.getLogger(ReviewPlanServiceImpl.class);

    @Override
    public List<ReviewDTO> getTodayReviews() {
        // 获取今天及之前未复习的任务
        LocalDate today = LocalDate.now();
        log.info("Querying review plans for date: {}", today);
        List<ReviewPlan> plans = reviewPlanRepository
                .findByScheduledDateLessThanEqualAndStatusWithKnowledge(today, ReviewPlan.ReviewStatus.PENDING);
        log.info("Found {} review plans for today", plans.size());
        
        return plans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDTO updateReviewStatus(Long reviewId, boolean mastered) {
        ReviewPlan plan = reviewPlanRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("复习计划不存在: " + reviewId));
        
        if (mastered) {
            // 掌握了，标记为完成
            plan.setStatus(ReviewPlan.ReviewStatus.COMPLETED);
            plan.setActualDate(LocalDate.now());
        } else {
            // 没有掌握，标记为失败，并安排第二天继续复习
            plan.setStatus(ReviewPlan.ReviewStatus.FAILED);
            plan.setActualDate(LocalDate.now());
            
            // 创建一个新的复习任务
            ReviewPlan newPlan = new ReviewPlan();
            newPlan.setKnowledge(plan.getKnowledge());
            newPlan.setReviewStage(plan.getReviewStage());
            newPlan.setScheduledDate(LocalDate.now().plusDays(1));
            newPlan.setStatus(ReviewPlan.ReviewStatus.PENDING);
            reviewPlanRepository.save(newPlan);
        }
        
        reviewPlanRepository.save(plan);
        return convertToDTO(plan);
    }

    @Override
    public Optional<ReviewPlan> findById(Long id) {
        return reviewPlanRepository.findById(id);
    }
    
    // 转换为DTO
    private ReviewDTO convertToDTO(ReviewPlan plan) {
        Knowledge knowledge = plan.getKnowledge();
        return new ReviewDTO(
                plan.getId(),
                knowledge.getId(),
                knowledge.getTitle(),
                knowledge.getOutline(),
                knowledge.getContent(),
                plan.getReviewStage(),
                plan.getScheduledDate(),
                plan.getStatus()
        );
    }
}