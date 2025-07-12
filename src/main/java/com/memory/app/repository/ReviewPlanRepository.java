package com.memory.app.repository;

import com.memory.app.model.ReviewPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReviewPlanRepository extends JpaRepository<ReviewPlan, Long> {

    @Query("SELECT rp FROM ReviewPlan rp JOIN rp.knowledge k WHERE k.id = :knowledgeId")
    List<ReviewPlan> findByKnowledgeId(@Param("knowledgeId") Long knowledgeId);
    
    List<ReviewPlan> findByScheduledDateAndStatus(LocalDate date, ReviewPlan.ReviewStatus status);
    
    @Query("SELECT rp FROM ReviewPlan rp WHERE rp.scheduledDate <= :date AND rp.status = :status")
    List<ReviewPlan> findByScheduledDateLessThanEqualAndStatus(LocalDate date, ReviewPlan.ReviewStatus status);
    
    @Query("SELECT rp FROM ReviewPlan rp JOIN FETCH rp.knowledge WHERE rp.scheduledDate = :date AND rp.status = :status")
    List<ReviewPlan> findByScheduledDateLessThanEqualAndStatusWithKnowledge(LocalDate date, ReviewPlan.ReviewStatus status);
    
    @Query("SELECT rp FROM ReviewPlan rp JOIN rp.knowledge k WHERE k.id = :knowledgeId AND rp.status = :status ORDER BY rp.actualDate DESC")
    List<ReviewPlan> findByKnowledgeIdAndStatusOrderByActualDateDesc(@Param("knowledgeId") Long knowledgeId, @Param("status") ReviewPlan.ReviewStatus status);
    
    @Query("SELECT rp FROM ReviewPlan rp JOIN rp.knowledge k WHERE k.id = :knowledgeId AND rp.status = :status ORDER BY rp.scheduledDate ASC")
    List<ReviewPlan> findByKnowledgeIdAndStatusOrderByScheduledDateAsc(@Param("knowledgeId") Long knowledgeId, @Param("status") ReviewPlan.ReviewStatus status);
    
    @Query("DELETE FROM ReviewPlan rp WHERE rp.knowledge.id = :knowledgeId")
    @Modifying
    void deleteByKnowledgeId(@Param("knowledgeId") Long knowledgeId);
    
    @Query("SELECT rp FROM ReviewPlan rp JOIN FETCH rp.knowledge WHERE rp.scheduledDate BETWEEN :startDate AND :endDate ORDER BY rp.scheduledDate ASC")
    List<ReviewPlan> findByScheduledDateBetweenWithKnowledge(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}