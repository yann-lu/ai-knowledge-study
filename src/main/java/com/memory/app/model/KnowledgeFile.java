package com.memory.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

/**
 * 知识文件上传类，用于处理文件上传并从文件中提取知识内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeFile {
    
    private MultipartFile file;
    private String title;
    private boolean generateOutline = true;
    private boolean generateReviewPlan = false;
    private Set<Long> categoryIds = new HashSet<>();
    
    /**
     * 获取文件名（不含扩展名）作为标题
     * 
     * @return 从文件名派生的标题
     */
    public String getTitleFromFilename() {
        if (file != null && file.getOriginalFilename() != null) {
            String filename = file.getOriginalFilename();
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return filename.substring(0, lastDotIndex);
            }
            return filename;
        }
        return null;
    }
    
    /**
     * 获取文件扩展名
     * 
     * @return 文件扩展名（不含点）
     */
    public String getFileExtension() {
        if (file != null && file.getOriginalFilename() != null) {
            String filename = file.getOriginalFilename();
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
                return filename.substring(lastDotIndex + 1).toLowerCase();
            }
        }
        return "";
    }
    
    /**
     * 检查文件是否为空
     * 
     * @return 如果文件为空则返回true
     */
    public boolean isFileEmpty() {
        return file == null || file.isEmpty();
    }
} 