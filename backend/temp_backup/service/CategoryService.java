package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;
    
    // CRUD Operations
    
    public CategoryDto.CategoryResponse createCategory(CategoryDto.CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        
        // Validate parent category if provided
        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentCategoryId()));
            
            if (!parentCategory.getIsActive()) {
                throw new ValidationException("Cannot create subcategory under inactive parent category");
            }
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(request.getParentCategoryId())
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        // Clear cache
        cacheService.evictCategoryCache(savedCategory.getId());
        if (savedCategory.getParentCategory() != null) {
            cacheService.evictCategoryCache(savedCategory.getParentCategory());
        }
        
        return mapToCategoryResponse(savedCategory);
    }
    
    public CategoryDto.CategoryResponse getCategoryById(String id) {
        log.debug("Fetching category by ID: {}", id);
        
        // Try cache first
        CategoryDto.CategoryResponse cached = cacheService.getCategoryFromCache(id);
        if (cached != null) {
            return cached;
        }
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        CategoryDto.CategoryResponse response = mapToCategoryResponse(category);
        
        // Cache the result
        cacheService.cacheCategory(id, response);
        
        return response;
    }
    
    public Page<CategoryDto.CategoryResponse> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories with pagination");
        
        Page<Category> categories = categoryRepository.findByIsActiveTrue(pageable);
        return categories.map(this::mapToCategoryResponse);
    }
    
    public CategoryDto.CategoryResponse updateCategory(String id, CategoryDto.UpdateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Validate parent category if changed
        if (request.getParentCategoryId() != null && !request.getParentCategoryId().equals(category.getParentCategory())) {
            if (!request.getParentCategoryId().equals("")) {
                Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentCategoryId()));
                
                // Prevent circular reference
                if (isCircularReference(id, request.getParentCategoryId())) {
                    throw new ValidationException("Cannot set parent category: would create circular reference");
                }
            }
        }
        
        // Update fields
        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getParentCategoryId() != null) {
            category.setParentCategory(request.getParentCategoryId().isEmpty() ? null : request.getParentCategoryId());
        }
        if (request.getImageUrl() != null) category.setImageUrl(request.getImageUrl());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        
        category.setUpdatedAt(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", savedCategory.getId());
        
        // Clear cache
        cacheService.evictCategoryCache(id);
        if (savedCategory.getParentCategory() != null) {
            cacheService.evictCategoryCache(savedCategory.getParentCategory());
        }
        
        return mapToCategoryResponse(savedCategory);
    }
    
    public void deleteCategory(String id) {
        log.info("Deleting category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Check if category has subcategories
        List<Category> subcategories = categoryRepository.findByParentCategoryAndIsActiveTrue(id);
        if (!subcategories.isEmpty()) {
            throw new ValidationException("Cannot delete category with active subcategories");
        }
        
        // Check if category has products (this would need to be implemented in ProductRepository)
        // For now, we'll do a soft delete
        
        // Soft delete
        category.setIsActive(false);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        // Clear cache
        cacheService.evictCategoryCache(id);
        if (category.getParentCategory() != null) {
            cacheService.evictCategoryCache(category.getParentCategory());
        }
        
        log.info("Category deleted successfully with ID: {}", id);
    }
    
    // Hierarchical Category Management
    
    public List<CategoryDto.CategoryResponse> getRootCategories() {
        log.debug("Fetching root categories");
        
        // Try cache first
        List<CategoryDto.CategoryResponse> cached = cacheService.getRootCategoriesFromCache();
        if (cached != null) {
            return cached;
        }
        
        List<Category> categories = categoryRepository.findByParentCategoryIsNullAndIsActiveTrueOrderBySortOrderAsc();
        
        List<CategoryDto.CategoryResponse> response = categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
        
        // Cache the result
        cacheService.cacheRootCategories(response);
        
        return response;
    }
    
    public List<CategoryDto.CategoryResponse> getSubcategories(String parentCategoryId) {
        log.debug("Fetching subcategories for parent ID: {}", parentCategoryId);
        
        List<Category> categories = categoryRepository.findByParentCategoryAndIsActiveTrueOrderBySortOrderAsc(parentCategoryId);
        
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    public CategoryDto.CategoryTreeResponse getCategoryTree() {
        log.debug("Building category tree");
        
        // Try cache first
        CategoryDto.CategoryTreeResponse cached = cacheService.getCategoryTreeFromCache();
        if (cached != null) {
            return cached;
        }
        
        List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        Map<String, List<Category>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(category -> 
                    category.getParentCategory() != null ? category.getParentCategory() : "root"));
        
        List<CategoryDto.CategoryTreeNode> rootNodes = buildCategoryTree("root", categoryMap);
        
        CategoryDto.CategoryTreeResponse response = CategoryDto.CategoryTreeResponse.builder()
                .categories(rootNodes)
                .totalCategories(allCategories.size())
                .build();
        
        // Cache the result
        cacheService.cacheCategoryTree(response);
        
        return response;
    }
    
    public List<CategoryDto.CategoryResponse> getCategoryPath(String categoryId) {
        log.debug("Getting category path for ID: {}", categoryId);
        
        List<CategoryDto.CategoryResponse> path = new ArrayList<>();
        String currentId = categoryId;
        
        while (currentId != null) {
            Category category = categoryRepository.findById(currentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + currentId));
            
            path.add(0, mapToCategoryResponse(category)); // Add to beginning
            currentId = category.getParentCategory();
        }
        
        return path;
    }
    
    public List<CategoryDto.CategoryResponse> getAllSubcategories(String categoryId) {
        log.debug("Getting all subcategories for category ID: {}", categoryId);
        
        List<CategoryDto.CategoryResponse> allSubcategories = new ArrayList<>();
        collectAllSubcategories(categoryId, allSubcategories);
        
        return allSubcategories;
    }
    
    // Category Tree Operations
    
    public CategoryDto.CategoryResponse moveCategory(String categoryId, String newParentId) {
        log.info("Moving category {} to new parent {}", categoryId, newParentId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
        
        // Validate new parent
        if (newParentId != null && !newParentId.isEmpty()) {
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("New parent category not found with ID: " + newParentId));
            
            // Prevent circular reference
            if (isCircularReference(categoryId, newParentId)) {
                throw new ValidationException("Cannot move category: would create circular reference");
            }
        }
        
        String oldParentId = category.getParentCategory();
        category.setParentCategory(newParentId != null && !newParentId.isEmpty() ? newParentId : null);
        category.setUpdatedAt(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        
        // Clear cache
        cacheService.evictCategoryCache(categoryId);
        cacheService.evictCategoryCache(oldParentId);
        cacheService.evictCategoryCache(newParentId);
        cacheService.evictCategoryTreeCache();
        
        log.info("Category moved successfully");
        return mapToCategoryResponse(savedCategory);
    }
    
    public CategoryDto.CategoryResponse updateCategoryOrder(String categoryId, Integer newSortOrder) {
        log.info("Updating sort order for category {} to {}", categoryId, newSortOrder);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
        
        category.setSortOrder(newSortOrder);
        category.setUpdatedAt(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        
        // Clear cache
        cacheService.evictCategoryCache(categoryId);
        cacheService.evictCategoryTreeCache();
        
        log.info("Category sort order updated successfully");
        return mapToCategoryResponse(savedCategory);
    }
    
    // Search and Filter Operations
    
    public List<CategoryDto.CategoryResponse> searchCategories(String query) {
        log.debug("Searching categories with query: {}", query);
        
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query);
        
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDto.CategoryResponse> getActiveCategories() {
        log.debug("Fetching all active categories");
        
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    // Utility Methods
    
    private List<CategoryDto.CategoryTreeNode> buildCategoryTree(String parentId, Map<String, List<Category>> categoryMap) {
        List<Category> children = categoryMap.getOrDefault(parentId, Collections.emptyList());
        
        return children.stream()
                .map(category -> {
                    List<CategoryDto.CategoryTreeNode> subcategories = buildCategoryTree(category.getId(), categoryMap);
                    return CategoryDto.CategoryTreeNode.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .imageUrl(category.getImageUrl())
                            .sortOrder(category.getSortOrder())
                            .subcategories(subcategories)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private void collectAllSubcategories(String categoryId, List<CategoryDto.CategoryResponse> allSubcategories) {
        List<Category> directSubcategories = categoryRepository.findByParentCategoryAndIsActiveTrueOrderBySortOrderAsc(categoryId);
        
        for (Category subcategory : directSubcategories) {
            allSubcategories.add(mapToCategoryResponse(subcategory));
            collectAllSubcategories(subcategory.getId(), allSubcategories);
        }
    }
    
    private boolean isCircularReference(String categoryId, String parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }
        
        String currentId = parentId;
        while (currentId != null) {
            Category category = categoryRepository.findById(currentId).orElse(null);
            if (category == null) {
                break;
            }
            
            if (categoryId.equals(category.getParentCategory())) {
                return true;
            }
            
            currentId = category.getParentCategory();
        }
        
        return false;
    }
    
    private CategoryDto.CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryDto.CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategory(category.getParentCategory())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
