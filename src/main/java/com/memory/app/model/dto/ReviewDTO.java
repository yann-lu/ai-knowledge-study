package com.memory.app.model.dto;

import com.memory.app.model.ReviewPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long knowledgeId;
    private String title;
    private String outline;
    private String content;
    private Integer reviewStage;
    private LocalDate scheduledDate;
    private ReviewPlan.ReviewStatus status;
} 