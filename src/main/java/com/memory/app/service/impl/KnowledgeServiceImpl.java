package com.memory.app.service.impl;

import com.memory.app.model.Category;
import com.memory.app.model.Knowledge;
import com.memory.app.model.KnowledgeFile;
import com.memory.app.model.ReviewPlan;
import com.memory.app.model.dto.KnowledgeDTO;
import com.memory.app.repository.CategoryRepository;
import com.memory.app.repository.KnowledgeRepository;
import com.memory.app.repository.ReviewPlanRepository;
import com.memory.app.service.FileParserService;
import com.memory.app.service.KnowledgeService;
import com.memory.app.service.ReviewPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final ReviewPlanService reviewPlanService;
    private final ReviewPlanRepository reviewPlanRepository;
    private final ChatModel chatLanguageModel;
    private final FileParserService fileParserService;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Knowledge save(KnowledgeDTO knowledgeDTO) {
        Knowledge knowledge = new Knowledge();
        
        if (knowledgeDTO.getId() != null) {
            knowledge = knowledgeRepository.findById(knowledgeDTO.getId())
                    .orElse(new Knowledge());
        }
        
        knowledge.setTitle(knowledgeDTO.getTitle());
        knowledge.setContent(knowledgeDTO.getContent());
        
        // 如果没有提供大纲，则自动生成
        if (knowledgeDTO.getOutline() == null || knowledgeDTO.getOutline().isEmpty()) {
            knowledge.setOutline(generateOutline(knowledgeDTO.getContent()));
        } else {
            knowledge.setOutline(knowledgeDTO.getOutline());
        }
        
        // 处理分类关联
        if (knowledgeDTO.getCategoryIds() != null && !knowledgeDTO.getCategoryIds().isEmpty()) {
            Set<Category> categories = knowledgeDTO.getCategoryIds().stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id)))
                    .collect(Collectors.toSet());
            
            // 清除旧分类并添加新分类
            knowledge.getCategories().clear();
            for (Category category : categories) {
                knowledge.addCategory(category);
            }
        } else {
            // 如果没有提供分类ID，则清空分类
            knowledge.getCategories().clear();
        }
        
        knowledge = knowledgeRepository.save(knowledge);
        
        // 如果需要生成复习计划
        if (knowledgeDTO.isGenerateReviewPlan()) {
            reviewPlanService.generateReviewPlan(knowledge);
        }
        
        return knowledge;
    }
    
    @Override
    @Transactional
    public Knowledge updateKnowledge(Knowledge knowledge) {
        return knowledgeRepository.save(knowledge);
    }

    @Override
    public List<Knowledge> findAll() {
        return knowledgeRepository.findAllByOrderByCreateTimeDesc();
    }

    @Override
    public Optional<Knowledge> findById(Long id) {
        return knowledgeRepository.findById(id);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        // 检查知识点是否存在
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + id));
        
        // 先删除相关的复习计划数据
        reviewPlanRepository.deleteByKnowledgeId(id);
        
        // 再删除知识点
        knowledgeRepository.deleteById(id);
    }

    @Override
    public String generateOutline(String content) {
        // 使用LangChain4j调用AI生成大纲
        String systemPromit = "请为一段长知识要点提炼简洁提示大纲. 分小点呈现，方便通过大纲回忆背诵的知识点​ 2. 可参考内容中的小标题来设计要点​ 3. 大纲需简短，能起到提示回忆的作用 4. 不要带需要我背诵的内容，只要有标题起到分类提醒既可";
        String prompt = systemPromit + "\n\n" + content;
        
        try {
            // LangChain4j调用AI生成大纲
            return chatLanguageModel.chat(UserMessage.from(prompt)).aiMessage().text();
        } catch (Exception e) {
            // 如果调用失败，生成一个简单的大纲
            return "内容大纲（自动生成失败）";
        }
    }
    
    @Override
    @Transactional
    public Knowledge createFromFile(KnowledgeFile knowledgeFile) throws Exception {
        // 检查文件是否为空
        if (knowledgeFile.isFileEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件类型是否支持
        if (!fileParserService.isSupportedFileType(knowledgeFile.getFile())) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
        
        // 提取文件内容
        String content = fileParserService.extractText(knowledgeFile.getFile());
        
        // 如果没有提供标题，则使用文件名作为标题
        String title = knowledgeFile.getTitle();
        if (title == null || title.isEmpty()) {
            title = knowledgeFile.getTitleFromFilename();
            if (title == null || title.isEmpty()) {
                title = "来自文件的知识点"; // 默认标题
            }
        }
        
        // 创建知识点
        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(title);
        knowledge.setContent(content);
        
        // 生成大纲（如果需要）
        if (knowledgeFile.isGenerateOutline()) {
            knowledge.setOutline(generateOutline(content));
        }
        
        // 保存知识点
        knowledge = knowledgeRepository.save(knowledge);
        
        // 如果需要生成复习计划
        if (knowledgeFile.isGenerateReviewPlan()) {
            reviewPlanService.generateReviewPlan(knowledge);
        }
        
        return knowledge;
    }

    @Override
    public List<Knowledge> findByCategory(Long categoryId) {
        return knowledgeRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Knowledge> findByCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return findAll();
        }
        return knowledgeRepository.findByCategoryIds(categoryIds, categoryIds.size());
    }

    @Override
    @Transactional
    public Knowledge addCategories(Long knowledgeId, Set<Long> categoryIds) {
        Knowledge knowledge = knowledgeRepository.findById(knowledgeId)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + knowledgeId));
        
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categories = categoryIds.stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id)))
                    .collect(Collectors.toSet());
            
            // 清除旧分类并添加新分类
            knowledge.getCategories().clear();
            for (Category category : categories) {
                knowledge.addCategory(category);
            }
        } else {
            // 如果没有提供分类ID，则清空分类
            knowledge.getCategories().clear();
        }
        
        return knowledgeRepository.save(knowledge);
    }

    @Override
    @Transactional
    public Knowledge removeCategories(Long knowledgeId, Set<Long> categoryIds) {
        Knowledge knowledge = knowledgeRepository.findById(knowledgeId)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + knowledgeId));
        
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categoriesToRemove = new HashSet<>();
            
            for (Long categoryId : categoryIds) {
                for (Category category : knowledge.getCategories()) {
                    if (category.getId().equals(categoryId)) {
                        categoriesToRemove.add(category);
                        break;
                    }
                }
            }
            
            for (Category category : categoriesToRemove) {
                knowledge.removeCategory(category);
            }
            
            return knowledgeRepository.save(knowledge);
        }
        
        return knowledge;
    }
    
    @Override
    public String getReviewedDates(Long knowledgeId) {
        List<ReviewPlan> completedReviews = reviewPlanRepository.findByKnowledgeIdAndStatusOrderByActualDateDesc(
                knowledgeId, ReviewPlan.ReviewStatus.COMPLETED);
        
        if (completedReviews.isEmpty()) {
            return "0次";
        }
        
        return completedReviews.size() + "次";
    }
    
    @Override
    public void markAsNotMastered(Long id) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + id));
        reviewPlanService.handleNotMastered(id);
    }

    @Override
    public void markAsMastered(Long id) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识点不存在: " + id));
        // 标记为已掌握，这里可以调用相关的复习计划更新逻辑
        // 暂时留空，因为ReviewPlanService中没有对应的handleMastered方法
    }

    @Override
    public String getUpcomingDates(Long knowledgeId) {
        List<ReviewPlan> pendingReviews = reviewPlanRepository.findByKnowledgeIdAndStatusOrderByScheduledDateAsc(
                knowledgeId, ReviewPlan.ReviewStatus.PENDING);
        
        if (pendingReviews.isEmpty()) {
            return "无";
        }
        
        // 返回最近的3个待复习日期
        return pendingReviews.stream()
                .limit(3)
                .map(review -> review.getScheduledDate().toString())
                .collect(Collectors.joining(", "));
    }
    
    @Override
    public List<Knowledge> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return knowledgeRepository.findByKeyword(keyword.trim());
    }
    
    @Override
    public List<Knowledge> searchByCategoryAndKeyword(Long categoryId, String keyword) {
        if (categoryId == null) {
            return searchByKeyword(keyword);
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return findByCategory(categoryId);
        }
        return knowledgeRepository.findByCategoryAndKeyword(categoryId, keyword.trim());
    }
    
    @Override
    public List<Knowledge> searchByCategoriesAndKeyword(Set<Long> categoryIds, String keyword) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return searchByKeyword(keyword);
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return findByCategories(categoryIds);
        }
        return knowledgeRepository.findByCategoriesAndKeyword(categoryIds, categoryIds.size(), keyword.trim());
    }
    
    @Override
    public Map<String, Object> getReviewStatus(Long knowledgeId) {
        Map<String, Object> status = new HashMap<>();
        
        // 获取所有复习计划
        List<ReviewPlan> allPlans = reviewPlanRepository.findByKnowledgeId(knowledgeId);
        
        if (allPlans.isEmpty()) {
            // 没有生成复习计划
            status.put("hasReviewPlan", false);
            status.put("reviewStatus", "未生成复习计划");
            status.put("completedCount", 0);
            status.put("totalCount", 0);
            status.put("pendingCount", 0);
            status.put("failedCount", 0);
        } else {
            // 已生成复习计划
            status.put("hasReviewPlan", true);
            
            // 统计各种状态的复习计划数量
            long completedCount = allPlans.stream().filter(p -> p.getStatus() == ReviewPlan.ReviewStatus.COMPLETED).count();
            long pendingCount = allPlans.stream().filter(p -> p.getStatus() == ReviewPlan.ReviewStatus.PENDING).count();
            long failedCount = allPlans.stream().filter(p -> p.getStatus() == ReviewPlan.ReviewStatus.FAILED).count();
            
            status.put("completedCount", completedCount);
            status.put("totalCount", allPlans.size());
            status.put("pendingCount", pendingCount);
            status.put("failedCount", failedCount);
            
            // 判断复习状态
            if (pendingCount == 0 && failedCount == 0 && completedCount > 0) {
                // 所有复习计划都已完成且掌握
                status.put("reviewStatus", "已掌握");
            } else if (pendingCount > 0 || failedCount > 0) {
                // 还有待复习或未掌握的
                status.put("reviewStatus", "进行中");
            } else {
                // 其他情况
                status.put("reviewStatus", "进行中");
            }
        }
        
        return status;
    }
    
    @Override
    public Page<Knowledge> findAllWithPagination(Pageable pageable) {
        Page<Knowledge> page = knowledgeRepository.findAllByOrderByCreateTimeDesc(pageable);
        // 预加载分类信息以避免N+1查询
        page.getContent().forEach(knowledge -> knowledge.getCategories().size());
        return page;
    }
    
    @Override
    public Page<Knowledge> searchWithPagination(String keyword, Long categoryId, Set<Long> categoryIds, Pageable pageable) {
        Page<Knowledge> page;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            // 多分类搜索
            if (keyword == null || keyword.trim().isEmpty()) {
                // 只按分类搜索
                page = knowledgeRepository.findByCategoryIds(categoryIds, categoryIds.size(), pageable);
            } else {
                page = knowledgeRepository.findByCategoriesAndKeyword(categoryIds, categoryIds.size(), keyword.trim(), pageable);
            }
        } else if (categoryId != null) {
            // 单分类搜索
            if (keyword == null || keyword.trim().isEmpty()) {
                // 只按分类搜索
                page = knowledgeRepository.findByCategoryId(categoryId, pageable);
            } else {
                page = knowledgeRepository.findByCategoryAndKeyword(categoryId, keyword.trim(), pageable);
            }
        } else {
            // 仅关键词搜索或查询所有
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAllWithPagination(pageable);
            } else {
                page = knowledgeRepository.findByKeyword(keyword.trim(), pageable);
            }
        }
        // 预加载分类信息以避免N+1查询
        page.getContent().forEach(knowledge -> knowledge.getCategories().size());
        return page;
    }
}