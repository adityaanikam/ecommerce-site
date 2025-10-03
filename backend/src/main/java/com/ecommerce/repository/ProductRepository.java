package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findBySubcategory(String subcategory, Pageable pageable);
    Page<Product> findByCategoryAndSubcategory(String category, String subcategory, Pageable pageable);
    
    @Query("{'price': {$gte: ?0, $lte: ?1}}")
    Page<Product> findByPriceRange(double minPrice, double maxPrice, Pageable pageable);
    
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    List<Product> findByIsFeatured(boolean isFeatured);
    List<Product> findByIsNew(boolean isNew);
    List<Product> findByIsOnSale(boolean isOnSale);
    
    Optional<Product> findBySlug(String slug);
    
    long countByCategory(String category);
}
