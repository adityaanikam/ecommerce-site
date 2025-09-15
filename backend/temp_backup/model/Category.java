package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {
    
    @Id
    private String id;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Indexed(unique = true)
    private String name;
    
    @Size(max = 500, message = "Category description cannot exceed 500 characters")
    private String description;
    
    @Field("parent_category")
    private String parentCategory;
    
    @Builder.Default
    @Field("is_active")
    private Boolean isActive = true;
    
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Field("image_url")
    private String imageUrl;
    
    @Builder.Default
    @Field("sort_order")
    private Integer sortOrder = 0;
    
    private List<String> subcategories;
    
    @Field("meta_title")
    @Size(max = 60, message = "Meta title cannot exceed 60 characters")
    private String metaTitle;
    
    @Field("meta_description")
    @Size(max = 160, message = "Meta description cannot exceed 160 characters")
    private String metaDescription;
    
    @Field("meta_keywords")
    private List<String> metaKeywords;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("created_by")
    private String createdBy;
    
    @Field("updated_by")
    private String updatedBy;
}