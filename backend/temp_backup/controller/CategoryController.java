package com.ecommerce.controller;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    // Public Category Operations
    
    @GetMapping
    public ResponseEntity<Page<CategoryDto.CategoryResponse>> getAllCategories(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Get all categories request received");
        Page<CategoryDto.CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto.CategoryResponse> getCategoryById(@PathVariable String categoryId) {
        log.info("Get category by ID request received: {}", categoryId);
        CategoryDto.CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getRootCategories() {
        log.info("Get root categories request received");
        List<CategoryDto.CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getSubcategories(@PathVariable String categoryId) {
        log.info("Get subcategories request received for category ID: {}", categoryId);
        List<CategoryDto.CategoryResponse> subcategories = categoryService.getSubcategories(categoryId);
        return ResponseEntity.ok(subcategories);
    }
    
    @GetMapping("/tree")
    public ResponseEntity<CategoryDto.CategoryTreeResponse> getCategoryTree() {
        log.info("Get category tree request received");
        CategoryDto.CategoryTreeResponse tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }
    
    @GetMapping("/{categoryId}/path")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getCategoryPath(@PathVariable String categoryId) {
        log.info("Get category path request received for category ID: {}", categoryId);
        List<CategoryDto.CategoryResponse> path = categoryService.getCategoryPath(categoryId);
        return ResponseEntity.ok(path);
    }
    
    @GetMapping("/{categoryId}/all-subcategories")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getAllSubcategories(@PathVariable String categoryId) {
        log.info("Get all subcategories request received for category ID: {}", categoryId);
        List<CategoryDto.CategoryResponse> subcategories = categoryService.getAllSubcategories(categoryId);
        return ResponseEntity.ok(subcategories);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> searchCategories(@RequestParam String query) {
        log.info("Search categories request received with query: {}", query);
        List<CategoryDto.CategoryResponse> categories = categoryService.searchCategories(query);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<CategoryDto.CategoryResponse>> getActiveCategories() {
        log.info("Get active categories request received");
        List<CategoryDto.CategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Category Management (Admin)
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> createCategory(@Valid @RequestBody CategoryDto.CreateCategoryRequest request) {
        log.info("Create category request received: {}", request.getName());
        CategoryDto.CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> updateCategory(@PathVariable String categoryId,
                                                                     @Valid @RequestBody CategoryDto.UpdateCategoryRequest request) {
        log.info("Update category request received for category ID: {}", categoryId);
        CategoryDto.CategoryResponse category = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(category);
    }
    
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.MessageResponse> deleteCategory(@PathVariable String categoryId) {
        log.info("Delete category request received for category ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(CategoryDto.MessageResponse.builder()
                .message("Category deleted successfully")
                .build());
    }
    
    // Category Tree Operations
    
    @PutMapping("/{categoryId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> moveCategory(@PathVariable String categoryId,
                                                                  @RequestParam(required = false) String newParentId) {
        log.info("Move category request received for category ID: {} to parent: {}", categoryId, newParentId);
        CategoryDto.CategoryResponse category = categoryService.moveCategory(categoryId, newParentId);
        return ResponseEntity.ok(category);
    }
    
    @PutMapping("/{categoryId}/order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> updateCategoryOrder(@PathVariable String categoryId,
                                                                         @RequestParam Integer sortOrder) {
        log.info("Update category order request received for category ID: {} to order: {}", categoryId, sortOrder);
        CategoryDto.CategoryResponse category = categoryService.updateCategoryOrder(categoryId, sortOrder);
        return ResponseEntity.ok(category);
    }
    
    // Image Management
    
    @PostMapping("/{categoryId}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> uploadCategoryImage(@PathVariable String categoryId,
                                                                         @RequestParam("file") MultipartFile file) {
        log.info("Upload category image request received for category ID: {}", categoryId);
        // TODO: Implement image upload logic
        CategoryDto.CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }
    
    @DeleteMapping("/{categoryId}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto.CategoryResponse> removeCategoryImage(@PathVariable String categoryId) {
        log.info("Remove category image request received for category ID: {}", categoryId);
        // TODO: Implement image removal logic
        CategoryDto.CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }
}
