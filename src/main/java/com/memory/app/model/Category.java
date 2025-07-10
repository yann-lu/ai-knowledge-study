package com.memory.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();
    
    @Column(nullable = false)
    private Integer level;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @ManyToMany(mappedBy = "categories")
    private Set<Knowledge> knowledgeSet = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    // 添加子分类的便捷方法
    public void addChild(Category child) {
        child.setParent(this);
        child.setLevel(this.level + 1);
        this.children.add(child);
    }
    
    // 判断是否为顶级分类
    public boolean isTopLevel() {
        return parent == null;
    }
    
    // 获取完整分类路径名称
    public String getFullPath() {
        if (isTopLevel()) {
            return name;
        }
        
        StringBuilder path = new StringBuilder();
        Category current = this;
        while (current != null) {
            if (path.length() > 0) {
                path.insert(0, " > ");
            }
            path.insert(0, current.getName());
            current = current.getParent();
        }
        return path.toString();
    }
} 