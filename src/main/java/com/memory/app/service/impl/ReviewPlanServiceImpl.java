package com.memory.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.memory.app.config.ReviewStrategyConfig;
import com.memory.app.model.Knowledge;
import com.memory.app.model.ReviewPlan;
import com.memory.app.model.dto.ReviewDTO;
import com.memory.app.repository.ReviewPlanRepository;
import com.memory.app.service.ReviewPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewPlanServiceImpl implements ReviewPlanService {

    private final ReviewPlanRepository reviewPlanRepository;
    private final ReviewStrategyConfig reviewStrategyConfig;

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
        ReviewStrategyConfig.NotMasteredStrategy strategy = reviewStrategyConfig.getNotMastered();
        
        if (strategy == ReviewStrategyConfig.NotMasteredStrategy.RESTART_FROM_TOMORROW) {
            handleNotMasteredWithRestart(knowledgeId);
        } else if (strategy == ReviewStrategyConfig.NotMasteredStrategy.ADD_EXTRA_REVIEWS) {
            handleNotMasteredWithExtraReviews(knowledgeId);
        }
    }
    
    /**
     * 原策略：从明天重新开始复习计划
     */
    private void handleNotMasteredWithRestart(Long knowledgeId) {
        // 删除所有现有的复习计划
        List<ReviewPlan> existingPlans = reviewPlanRepository.findByKnowledgeId(knowledgeId);
        if (!existingPlans.isEmpty()) {
            reviewPlanRepository.deleteAll(existingPlans);
        }

        // 创建新的复习计划，从明天开始，重新生成10次复习
        // 艾宾浩斯记忆曲线的复习间隔（天数），从明天作为第一天开始
        int[] reviewIntervals = {1, 2, 4, 7, 14, 21, 30, 45, 60, 90}; // 10次复习间隔
        
        List<ReviewPlan> plans = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < reviewIntervals.length; i++) {
            ReviewPlan plan = new ReviewPlan();
            plan.setKnowledgeId(knowledgeId);
            plan.setReviewStage(i + 1); // 从第1阶段重新开始
            plan.setScheduledDate(today.plusDays(reviewIntervals[i])); // 从明天开始
            plan.setStatus(ReviewPlan.ReviewStatus.PENDING);
            plans.add(plan);
        }

        reviewPlanRepository.saveAll(plans);
        log.info("为知识点 {} 重新生成了 {} 次复习计划，从明天开始", knowledgeId, plans.size());
    }
    
    /**
     * 新策略：增加额外复习次数，原计划顺延
     */
    private void handleNotMasteredWithExtraReviews(Long knowledgeId) {
        List<ReviewPlan> existingPlans = reviewPlanRepository.findByKnowledgeId(knowledgeId);
        
        // 获取所有未完成的复习计划，按计划日期排序
        List<ReviewPlan> pendingPlans = existingPlans.stream()
                .filter(plan -> plan.getStatus() == ReviewPlan.ReviewStatus.PENDING)
                .sorted((p1, p2) -> p1.getScheduledDate().compareTo(p2.getScheduledDate()))
                .collect(Collectors.toList());
        
        LocalDate today = LocalDate.now();
        int extraReviewCount = reviewStrategyConfig.getExtraReviewCount();
        
        // 将所有未完成的复习计划顺延指定天数
        for (ReviewPlan plan : pendingPlans) {
            plan.setScheduledDate(plan.getScheduledDate().plusDays(extraReviewCount));
        }
        
        // 获取Knowledge对象用于创建额外复习计划
        Knowledge knowledge = null;
        if (!existingPlans.isEmpty()) {
            knowledge = existingPlans.get(0).getKnowledge();
        }
        
        // 创建额外的复习计划
        List<ReviewPlan> extraPlans = new ArrayList<>();
        for (int i = 1; i <= extraReviewCount; i++) {
            ReviewPlan extraPlan = new ReviewPlan();
            extraPlan.setKnowledge(knowledge);
            extraPlan.setReviewStage(0); // 标记为额外复习
            extraPlan.setScheduledDate(today.plusDays(i));
            extraPlan.setStatus(ReviewPlan.ReviewStatus.PENDING);
            extraPlans.add(extraPlan);
        }
        
        // 保存所有变更
        reviewPlanRepository.saveAll(pendingPlans);
        reviewPlanRepository.saveAll(extraPlans);
        
        log.info("为知识点 {} 增加了 {} 次额外复习，原有 {} 个复习计划顺延 {} 天", 
                knowledgeId, extraReviewCount, pendingPlans.size(), extraReviewCount);
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
            reviewPlanRepository.save(plan);
        } else {
            // 没有掌握，标记为失败
            plan.setStatus(ReviewPlan.ReviewStatus.FAILED);
            plan.setActualDate(LocalDate.now());
            Long knowledgeId = plan.getKnowledge().getId();
            
            // 先保存当前计划的失败状态
            reviewPlanRepository.save(plan);
            
            // 然后重新生成完整的10次复习计划，从明天开始
            handleNotMastered(knowledgeId);
            log.info("复习失败，为知识点 {} 重新生成复习计划", knowledgeId);
        }
        
        return convertToDTO(plan);
    }

    @Override
    public Optional<ReviewPlan> findById(Long id) {
        return reviewPlanRepository.findById(id);
    }
    
    @Override
    public List<ReviewDTO> getReviewPlansByKnowledgeId(Long knowledgeId) {
        List<ReviewPlan> plans = reviewPlanRepository.findByKnowledgeId(knowledgeId);
        return plans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void markAsCompleted(Long reviewId) {
        ReviewPlan plan = reviewPlanRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("复习计划不存在: " + reviewId));
        
        // 标记为已完成
        plan.setStatus(ReviewPlan.ReviewStatus.COMPLETED);
        plan.setActualDate(LocalDate.now());
        
        reviewPlanRepository.save(plan);
        log.info("复习计划已标记为完成: {}", reviewId);
    }
    
    @Override
    public Map<String, Object> getCalendarData(YearMonth yearMonth) {
        // 获取指定月份的第一天和最后一天
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 查询该月份的所有复习计划
        List<ReviewPlan> plans = reviewPlanRepository.findByScheduledDateBetweenWithKnowledge(startDate, endDate);
        
        // 按日期分组统计
        Map<String, Map<String, Object>> dailyData = new HashMap<>();
        
        for (ReviewPlan plan : plans) {
            String dateKey = plan.getScheduledDate().toString();
            
            dailyData.computeIfAbsent(dateKey, k -> {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("total", 0);
                dayData.put("completed", 0);
                dayData.put("failed", 0);
                dayData.put("pending", 0);
                dayData.put("reviews", new ArrayList<Map<String, Object>>());
                return dayData;
            });
            
            Map<String, Object> dayData = dailyData.get(dateKey);
            dayData.put("total", (Integer) dayData.get("total") + 1);
            
            // 统计各状态数量
            switch (plan.getStatus()) {
                case COMPLETED:
                    dayData.put("completed", (Integer) dayData.get("completed") + 1);
                    break;
                case FAILED:
                    dayData.put("failed", (Integer) dayData.get("failed") + 1);
                    break;
                case PENDING:
                    dayData.put("pending", (Integer) dayData.get("pending") + 1);
                    break;
            }
            
            // 添加复习详情
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> reviews = (List<Map<String, Object>>) dayData.get("reviews");
            Map<String, Object> reviewInfo = new HashMap<>();
            reviewInfo.put("id", plan.getId());
            reviewInfo.put("knowledgeId", plan.getKnowledge().getId());
            reviewInfo.put("title", plan.getKnowledge().getTitle());
            reviewInfo.put("reviewStage", plan.getReviewStage());
            reviewInfo.put("status", plan.getStatus().toString());
            reviews.add(reviewInfo);
        }
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("year", yearMonth.getYear());
        result.put("month", yearMonth.getMonthValue());
        result.put("dailyData", dailyData);
        
        // 计算月度统计
        int totalReviews = plans.size();
        long completedReviews = plans.stream().filter(p -> p.getStatus() == ReviewPlan.ReviewStatus.COMPLETED).count();
        long failedReviews = plans.stream().filter(p -> p.getStatus() == ReviewPlan.ReviewStatus.FAILED).count();
        long pendingReviews = plans.stream().filter(p -> p.getStatus() == ReviewPlan.ReviewStatus.PENDING).count();
        
        Map<String, Object> monthlyStats = new HashMap<>();
        monthlyStats.put("total", totalReviews);
        monthlyStats.put("completed", completedReviews);
        monthlyStats.put("failed", failedReviews);
        monthlyStats.put("pending", pendingReviews);
        
        result.put("monthlyStats", monthlyStats);
        
        return result;
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