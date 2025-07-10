package com.memory.app.controller;

import com.memory.app.model.dto.ReviewDTO;
import com.memory.app.service.ReviewPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewPlanService reviewPlanService;

    @GetMapping
    public String todayReviews(Model model) {
        List<ReviewDTO> todayReviews = reviewPlanService.getTodayReviews();
        model.addAttribute("reviewPlans", todayReviews);
        return "review/daily";
    }

    @PostMapping("/{id}/update")
    @ResponseBody
    public ReviewDTO updateReviewStatus(
            @PathVariable Long id,
            @RequestParam boolean mastered) {
        ReviewDTO result = reviewPlanService.updateReviewStatus(id, mastered);
        if (!mastered) {
            reviewPlanService.handleNotMastered(result.getKnowledgeId());
        }
        return result;
    }
}