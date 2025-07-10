package com.memory.app.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件解析服务接口
 */
public interface FileParserService {
    
    /**
     * 从文件中提取文本内容
     * 
     * @param file 上传的文件
     * @return 提取的文本内容
     * @throws Exception 如果文件解析失败
     */
    String extractText(MultipartFile file) throws Exception;
    
    /**
     * 检查文件是否为支持的类型
     * 
     * @param file 上传的文件
     * @return 如果文件类型受支持则返回true
     */
    boolean isSupportedFileType(MultipartFile file);
} 