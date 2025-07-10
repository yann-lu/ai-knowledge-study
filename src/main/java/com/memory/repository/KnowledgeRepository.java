package com.memory.repository;

import com.memory.model.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    List<Knowledge> findByCategoryIdIn(List<Long> categoryIds);
}