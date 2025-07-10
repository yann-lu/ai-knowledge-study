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
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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
        
        knowledge = knowledgeRepository.save(knowledge);
        
        // 如果需要生成复习计划
        if (knowledgeDTO.isGenerateReviewPlan()) {
            reviewPlanService.generateReviewPlan(knowledge);
        }
        
        return knowledge;
    }

    @Override
    public List<Knowledge> findAll() {
        return knowledgeRepository.findAll();
    }

    @Override
    public Optional<Knowledge> findById(Long id) {
        return knowledgeRepository.findById(id);
    }

    @Override
    public String generateOutline(String content) {
        // 使用LangChain4j调用AI生成大纲
        String prompt = "请为以下内容生成一个简洁的知识大纲，帮助记忆：\n\n" + content;
        
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
}