package com.ecommerce.repository;

import com.ecommerce.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    // Basic queries
    @Query("{'isActive': true}")
    List<Category> findAllByIsActiveTrue();
    
    @Query("{'isActive': true}")
    Page<Category> findByIsActiveTrue(Pageable pageable);
    
    // Name-based queries
    @Query("{'name': ?0, 'isActive': true}")
    Optional<Category> findByNameAndIsActiveTrue(String name);
    
    @Query("{'name': {$regex: ?0, $options: 'i'}, 'isActive': true}")
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    // Hierarchical queries
    @Query("{'parentCategory': null, 'isActive': true}")
    List<Category> findRootCategories();
    
    @Query("{'parentCategory': null, 'isActive': true}")
    Page<Category> findRootCategories(Pageable pageable);
    
    @Query("{'parentCategory': ?0, 'isActive': true}")
    List<Category> findByParentCategoryAndIsActiveTrue(String parentCategoryId);
    
    @Query("{'parentCategory': ?0, 'isActive': true}")
    Page<Category> findByParentCategoryAndIsActiveTrue(String parentCategoryId, Pageable pageable);
    
    // Subcategory queries
    @Query("{'subcategories': ?0, 'isActive': true}")
    List<Category> findBySubcategoriesContainingAndIsActiveTrue(String subcategory);
    
    @Query("{'subcategories': {$exists: true, $ne: []}, 'isActive': true}")
    List<Category> findCategoriesWithSubcategories();
    
    // Sort order queries
    @Query("{'isActive': true}")
    List<Category> findAllByIsActiveTrueOrderBySortOrderAsc();
    
    @Query("{'parentCategory': ?0, 'isActive': true}")
    List<Category> findByParentCategoryAndIsActiveTrueOrderBySortOrderAsc(String parentCategoryId);
    
    @Query("{'parentCategory': null, 'isActive': true}")
    List<Category> findRootCategoriesOrderBySortOrderAsc();
    
    // Search queries
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}], 'isActive': true}")
    List<Category> searchCategories(String searchTerm);
    
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}], 'isActive': true}")
    Page<Category> searchCategories(String searchTerm, Pageable pageable);
    
    // Meta data queries
    @Query("{'metaKeywords': {$in: [?0]}, 'isActive': true}")
    List<Category> findByMetaKeywordsContainingAndIsActiveTrue(String keyword);
    
    @Query("{'metaTitle': {$regex: ?0, $options: 'i'}, 'isActive': true}")
    List<Category> findByMetaTitleContainingIgnoreCaseAndIsActiveTrue(String metaTitle);
    
    // Statistics queries
    @Query(value = "{'isActive': true}", count = true)
    long countByIsActiveTrue();
    
    @Query(value = "{'parentCategory': null, 'isActive': true}", count = true)
    long countRootCategories();
    
    @Query(value = "{'parentCategory': ?0, 'isActive': true}", count = true)
    long countByParentCategoryAndIsActiveTrue(String parentCategoryId);
    
    // Audit queries
    @Query("{'createdBy': ?0, 'isActive': true}")
    List<Category> findByCreatedByAndIsActiveTrue(String createdBy);
    
    @Query("{'updatedBy': ?0, 'isActive': true}")
    List<Category> findByUpdatedByAndIsActiveTrue(String updatedBy);
    
    // Image queries
    @Query("{'imageUrl': {$exists: true, $ne: null}, 'isActive': true}")
    List<Category> findCategoriesWithImages();
    
    @Query("{'imageUrl': {$exists: false}, 'isActive': true}")
    List<Category> findCategoriesWithoutImages();
    
    // Recent categories
    @Query("{'isActive': true}")
    List<Category> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    
    // Categories with most subcategories
    @Query("{'subcategories': {$exists: true}, 'isActive': true}")
    List<Category> findCategoriesWithSubcategoriesOrderBySubcategoriesSizeDesc();
    
    // Find category path (breadcrumb)
    @Query("{'$or': [{'name': ?0}, {'subcategories': ?0}], 'isActive': true}")
    List<Category> findCategoryPath(String categoryName);
    
    // Find all descendants of a category
    @Query("{'$or': [{'parentCategory': ?0}, {'parentCategory': {$in: ?1}}], 'isActive': true}")
    List<Category> findAllDescendants(String categoryId, List<String> descendantIds);
    
    // Find categories by depth level
    @Query("{'parentCategory': null, 'isActive': true}")
    List<Category> findLevel1Categories();
    
    @Query("{'parentCategory': {$in: ?0}, 'isActive': true}")
    List<Category> findLevel2Categories(List<String> level1CategoryIds);
    
    // Find leaf categories (categories without subcategories)
    @Query("{'subcategories': {$exists: false}, 'isActive': true}")
    List<Category> findLeafCategories();
    
    @Query("{'subcategories': {$size: 0}, 'isActive': true}")
    List<Category> findLeafCategoriesWithEmptySubcategories();
}