package com.memory.app.controller;

import com.memory.app.model.Knowledge;
import com.memory.app.model.dto.ReviewDTO;
import com.memory.app.service.KnowledgeService;
import com.memory.app.service.ReviewPlanService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ApiReviewController.class);
    
    private final ReviewPlanService reviewPlanService;
    private final KnowledgeService knowledgeService;
    
    @GetMapping("/today")
    public ResponseEntity<List<ReviewDTO>> getTodayReviews() {
        List<ReviewDTO> todayReviews = reviewPlanService.getTodayReviews();
        return ResponseEntity.ok(todayReviews);
    }
    
    @GetMapping("/knowledge/{knowledgeId}")
    public ResponseEntity<List<ReviewDTO>> getReviewPlansByKnowledgeId(@PathVariable Long knowledgeId) {
        try {
            List<ReviewDTO> reviewPlans = reviewPlanService.getReviewPlansByKnowledgeId(knowledgeId);
            return ResponseEntity.ok(reviewPlans);
        } catch (Exception e) {
            logger.error("获取知识点复习计划失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean mastered = request.get("mastered");
            if (mastered == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少必要参数: mastered"));
            }
            
            ReviewDTO result = reviewPlanService.updateReviewStatus(id, mastered);
            
            // 如果未掌握，重新生成复习计划
            if (!mastered) {
                reviewPlanService.handleNotMastered(result.getKnowledgeId());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("更新复习状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新复习状态失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/knowledge/{knowledgeId}/generate")
    public ResponseEntity<?> generateReviewPlan(@PathVariable Long knowledgeId) {
        try {
            // 获取知识点对象
            Optional<Knowledge> knowledgeOpt = knowledgeService.findById(knowledgeId);
            if (!knowledgeOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "知识点不存在: " + knowledgeId));
            }
            
            // 生成完整的复习计划（包含第一天）
            reviewPlanService.generateReviewPlan(knowledgeOpt.get());
            return ResponseEntity.ok(Map.of("message", "复习计划生成成功"));
        } catch (Exception e) {
            logger.error("生成复习计划失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "生成复习计划失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> markReviewAsCompleted(@PathVariable Long id) {
        try {
            reviewPlanService.markAsCompleted(id);
            return ResponseEntity.ok(Map.of("message", "复习已标记为完成"));
        } catch (Exception e) {
            logger.error("标记复习完成失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "标记复习完成失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/calendar")
    public ResponseEntity<Map<String, Object>> getCalendarData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            // 如果没有指定年月，使用当前年月
            YearMonth targetMonth = (year != null && month != null) 
                ? YearMonth.of(year, month) 
                : YearMonth.now();
            
            Map<String, Object> calendarData = reviewPlanService.getCalendarData(targetMonth);
            return ResponseEntity.ok(calendarData);
        } catch (Exception e) {
            logger.error("获取日历数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取日历数据失败: " + e.getMessage()));
        }
    }
}