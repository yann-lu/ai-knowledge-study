package com.memory.app.service.impl;

import com.memory.app.service.FileParserService;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件解析服务实现类
 */
@Service
public class FileParserServiceImpl implements FileParserService {
    
    // 支持的文件类型
    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            "application/pdf",                         // PDF
            "application/msword",                      // DOC
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // DOCX
            "application/vnd.ms-powerpoint",           // PPT
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // PPTX
            "application/vnd.ms-excel",                // XLS
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // XLSX
            "text/plain",                              // TXT
            "text/html",                               // HTML
            "text/markdown",                           // MD
            "application/rtf"                          // RTF
    ));
    
    // 支持的文件扩展名
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "html", "htm", "md", "rtf"
    ));
    
    private final Tika tika = new Tika();
    
    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            // 使用Tika的自动检测解析器
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 表示没有文本长度限制
            Metadata metadata = new Metadata();
            
            // 设置文件名
            if (file.getOriginalFilename() != null) {
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());
            }
            
            // 解析文档
            parser.parse(inputStream, handler, metadata, new ParseContext());
            
            String content = handler.toString().trim();
            
            // 如果提取的内容太长，截取合理的长度
            if (content.length() > 10000) {
                content = content.substring(0, 10000) + "...\n[内容过长，已截断]";
            }
            
            return content;
        }
    }
    
    @Override
    public boolean isSupportedFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        try {
            // 检查MIME类型
            String contentType = tika.detect(file.getInputStream());
            if (SUPPORTED_TYPES.contains(contentType)) {
                return true;
            }
            
            // 检查文件扩展名
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.contains(".")) {
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                return SUPPORTED_EXTENSIONS.contains(extension);
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
} 