package com.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.model.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    
    private String id;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Category description cannot exceed 500 characters")
    private String description;
    
    private String parentCategoryId;
    private Category parentCategory;
    private List<Category> subcategories;
    private Boolean isActive;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Inner classes for request/response DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
        private String name;
        
        @Size(max = 500, message = "Category description cannot exceed 500 characters")
        private String description;
        
        private String parentCategoryId;
        private String imageUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCategoryRequest {
        @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
        private String name;
        
        @Size(max = 500, message = "Category description cannot exceed 500 characters")
        private String description;
        
        private String parentCategoryId;
        private String imageUrl;
        private Boolean isActive;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private String id;
        private String name;
        private String description;
        private String parentCategoryId;
        private Category parentCategory;
        private List<Category> subcategories;
        private Boolean isActive;
        private String imageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTreeResponse {
        private String id;
        private String name;
        private String description;
        private String imageUrl;
        private List<CategoryTreeResponse> children;
        private Integer productCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTreeNode {
        private String id;
        private String name;
        private String description;
        private String parentCategoryId;
        private Boolean isActive;
        private String imageUrl;
        private List<CategoryTreeNode> children;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
        private Object data;
    }
}