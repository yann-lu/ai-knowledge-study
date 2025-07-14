package com.memory.app.controller;

import com.memory.app.dto.CategoryDTO;
import com.memory.app.model.Category;
import com.memory.app.model.Knowledge;
import com.memory.app.model.dto.KnowledgeDTO;
import com.memory.app.service.CategoryService;
import com.memory.app.service.KnowledgeService;
import com.memory.app.util.MarkdownUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiKnowledgeController {

    private static final Logger logger = LoggerFactory.getLogger(ApiKnowledgeController.class);
    
    private final KnowledgeService knowledgeService;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Knowledge>> getAllKnowledge() {
        logger.info("Received request for /api/knowledge");
        List<Knowledge> knowledgeList = knowledgeService.findAll();
        logger.info("Returning {} knowledge items", knowledgeList.size());
        return ResponseEntity.ok(knowledgeList);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getKnowledgeById(@PathVariable Long id) {
        try {
            logger.info("Received request for /api/knowledge/{}", id);
            Optional<Knowledge> knowledge = knowledgeService.findById(id);
            if (knowledge.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("knowledge", knowledge.get());
                response.put("htmlContent", MarkdownUtils.convertToHtml(knowledge.get().getContent()));
                response.put("htmlOutline", MarkdownUtils.convertToHtml(knowledge.get().getOutline()));
                logger.info("Returning knowledge item with id {}", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Knowledge item with id {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "知识点不存在: " + id));
            }
        } catch (Exception e) {
            logger.error("获取知识点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取知识点失败: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createKnowledge(@RequestBody KnowledgeDTO knowledgeDTO) {
        try {
            logger.info("收到创建知识点请求: {}", knowledgeDTO);
            Knowledge savedKnowledge = knowledgeService.save(knowledgeDTO);
            logger.info("知识点创建成功: {}", savedKnowledge.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedKnowledge);
        } catch (Exception e) {
            logger.error("创建知识点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "创建知识点失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeDTO knowledgeDTO) {
        try {
            if (!knowledgeService.findById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "知识点不存在: " + id));
            }
            
            knowledgeDTO.setId(id); // 确保ID正确
            logger.info("收到更新知识点请求: {}", knowledgeDTO);
            Knowledge updatedKnowledge = knowledgeService.save(knowledgeDTO);
            logger.info("知识点更新成功: {}", updatedKnowledge.getTitle());
            return ResponseEntity.ok(updatedKnowledge);
        } catch (Exception e) {
            logger.error("更新知识点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新知识点失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKnowledge(@PathVariable Long id) {
        try {
            logger.info("收到删除知识点请求，ID: {}", id);
            
            // 检查知识点是否存在
            Optional<Knowledge> knowledgeOpt = knowledgeService.findById(id);
            if (!knowledgeOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "知识点不存在: " + id));
            }
            
            Knowledge knowledge = knowledgeOpt.get();
            String title = knowledge.getTitle();
            
            // 删除知识点（包括相关的复习计划数据）
            knowledgeService.deleteById(id);
            
            logger.info("知识点删除成功: {}", title);
            return ResponseEntity.ok(Map.of("message", "知识点删除成功: " + title));
        } catch (Exception e) {
            logger.error("删除知识点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除知识点失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/categories")
    public ResponseEntity<?> updateCategories(@PathVariable Long id, @RequestBody Set<Long> categoryIds) {
        try {
            Knowledge knowledge = knowledgeService.addCategories(id, categoryIds);
            return ResponseEntity.ok().body(knowledge);
        } catch (Exception e) {
            logger.error("更新分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "更新分类失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/generate-outline")
    public ResponseEntity<?> generateOutline(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            logger.info("收到生成大纲请求，内容长度: {}", content != null ? content.length() : 0);
            String outline = knowledgeService.generateOutline(content);
            return ResponseEntity.ok(Map.of("outline", outline));
        } catch (Exception e) {
            logger.error("生成大纲失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "生成大纲失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/regenerate-outline")
    public ResponseEntity<?> regenerateOutline(@PathVariable Long id) {
        try {
            logger.info("收到重新生成大纲请求，知识点ID: {}", id);
            
            // 检查知识点是否存在
            Optional<Knowledge> knowledgeOpt = knowledgeService.findById(id);
            if (!knowledgeOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "知识点不存在: " + id));
            }
            
            Knowledge knowledge = knowledgeOpt.get();
            String newOutline = knowledgeService.generateOutline(knowledge.getContent());
            
            // 更新知识点的大纲
            knowledge.setOutline(newOutline);
            Knowledge updatedKnowledge = knowledgeService.updateKnowledge(knowledge);
            
            logger.info("知识点大纲重新生成成功: {}", knowledge.getTitle());
            return ResponseEntity.ok(Map.of(
                "outline", newOutline, 
                "knowledge", updatedKnowledge,
                "htmlOutline", MarkdownUtils.convertToHtml(newOutline)
            ));
        } catch (Exception e) {
            logger.error("重新生成大纲失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "重新生成大纲失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/not-mastered")
    public ResponseEntity<?> markAsNotMastered(@PathVariable Long id) {
        try {
            knowledgeService.markAsNotMastered(id);
            return ResponseEntity.ok(Map.of("message", "成功标记为未掌握"));
        } catch (Exception e) {
            logger.error("标记未掌握失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "标记未掌握失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/mastered")
    public ResponseEntity<?> markAsMastered(@PathVariable Long id) {
        try {
            knowledgeService.markAsMastered(id);
            return ResponseEntity.ok(Map.of("message", "成功标记为已掌握"));
        } catch (Exception e) {
            logger.error("标记已掌握失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "标记已掌握失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/review-status")
    public ResponseEntity<?> getReviewStatus(@PathVariable Long id) {
        try {
            logger.info("Received request for /api/knowledge/{}/review-status", id);
            Map<String, Object> reviewStatus = knowledgeService.getReviewStatus(id);
            logger.info("Returning review status for knowledge id {}", id);
            return ResponseEntity.ok(reviewStatus);
        } catch (Exception e) {
            logger.error("获取复习状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取复习状态失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            logger.info("Received request for /api/knowledge/categories");
            List<CategoryDTO> categories = categoryService.findAllAsDTO();
            logger.info("Returning {} categories", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("获取分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取分类失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/categories/tree")
    public ResponseEntity<?> getCategoryTree() {
        try {
            logger.info("Received request for /api/knowledge/categories/tree");
            List<CategoryDTO> categoryTree = categoryService.getCategoryTree();
            logger.info("Returning category tree with {} top-level categories", categoryTree.size());
            return ResponseEntity.ok(categoryTree);
        } catch (Exception e) {
            logger.error("获取分类树失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取分类树失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchKnowledge(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Set<Long> categoryIds) {
        try {
            logger.info("Received search request - keyword: {}, categoryId: {}, categoryIds: {}", 
                       keyword, categoryId, categoryIds);
            
            List<Knowledge> knowledgeList;
            
            if (categoryIds != null && !categoryIds.isEmpty()) {
                // 多分类搜索
                knowledgeList = knowledgeService.searchByCategoriesAndKeyword(categoryIds, keyword);
            } else if (categoryId != null) {
                // 单分类搜索
                knowledgeList = knowledgeService.searchByCategoryAndKeyword(categoryId, keyword);
            } else {
                // 仅关键词搜索
                knowledgeList = knowledgeService.searchByKeyword(keyword);
            }
            
            logger.info("Returning {} knowledge items from search", knowledgeList.size());
            return ResponseEntity.ok(knowledgeList);
        } catch (Exception e) {
            logger.error("搜索知识点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "搜索知识点失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/page")
    public ResponseEntity<?> getKnowledgePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Set<Long> categoryIds) {
        try {
            logger.info("Received paginated request - page: {}, size: {}, keyword: {}, categoryId: {}, categoryIds: {}", 
                       page, size, keyword, categoryId, categoryIds);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Knowledge> knowledgePage;
            
            if (keyword != null || categoryId != null || (categoryIds != null && !categoryIds.isEmpty())) {
                // 有搜索条件，使用搜索分页
                knowledgePage = knowledgeService.searchWithPagination(keyword, categoryId, categoryIds, pageable);
            } else {
                // 无搜索条件，查询所有
                knowledgePage = knowledgeService.findAllWithPagination(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", knowledgePage.getContent());
            response.put("totalElements", knowledgePage.getTotalElements());
            response.put("totalPages", knowledgePage.getTotalPages());
            response.put("currentPage", knowledgePage.getNumber());
            response.put("pageSize", knowledgePage.getSize());
            response.put("hasNext", knowledgePage.hasNext());
            response.put("hasPrevious", knowledgePage.hasPrevious());
            
            logger.info("Returning paginated knowledge - total: {}, page: {}/{}", 
                       knowledgePage.getTotalElements(), page + 1, knowledgePage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("分页查询知识点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "分页查询知识点失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getKnowledgeStatistics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        try {
            logger.info("Received statistics request - page: {}, size: {}, keyword: {}, categoryId: {}", 
                       page, size, keyword, categoryId);
            
            // 获取知识点统计数据
            Map<String, Object> statistics = knowledgeService.getKnowledgeStatistics(keyword, categoryId);
            
            logger.info("Returning knowledge statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("获取知识点统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取知识点统计失败: " + e.getMessage()));
        }
    }
}