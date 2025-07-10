package com.memory.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_id", nullable = false)
    private Knowledge knowledge;
    
    @Column(name = "review_stage", nullable = false)
    private Integer reviewStage;
    
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;
    
    @Column(name = "actual_date")
    private LocalDate actualDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    public Long getKnowledgeId() {
        return knowledge != null ? knowledge.getId() : null;
    }
    
    public void setKnowledgeId(Long knowledgeId) {
        if (knowledge == null) {
            knowledge = new Knowledge();
        }
        knowledge.setId(knowledgeId);
    }
    
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    public enum ReviewStatus {
        PENDING,    // 待复习
        COMPLETED,  // 已完成且掌握
        FAILED      // 复习但未掌握
    }
} 