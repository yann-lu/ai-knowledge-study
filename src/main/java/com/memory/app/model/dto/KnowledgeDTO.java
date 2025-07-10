package com.memory.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDTO {
    private Long id;
    private String title;
    private String content;
    private String outline;
    private boolean generateReviewPlan;
    private Set<Long> categoryIds = new HashSet<>();
} 