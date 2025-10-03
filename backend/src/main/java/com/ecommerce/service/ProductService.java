package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<Product> findProducts(
            String category,
            String subcategory,
            String search,
            Double minPrice,
            Double maxPrice,
            PageRequest pageRequest
    ) {
        if (search != null && !search.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(search, pageRequest);
        }
        
        if (category != null && subcategory != null) {
            return productRepository.findByCategoryAndSubcategory(category, subcategory, pageRequest);
        }
        
        if (category != null) {
            return productRepository.findByCategory(category, pageRequest);
        }
        
        if (subcategory != null) {
            return productRepository.findBySubcategory(subcategory, pageRequest);
        }
        
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceRange(minPrice, maxPrice, pageRequest);
        }
        
        return productRepository.findAll(pageRequest);
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public Page<Product> findByCategory(String category, PageRequest pageRequest) {
        return productRepository.findByCategory(category, pageRequest);
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findByIsFeatured(true);
    }

    public List<Product> findNewProducts() {
        return productRepository.findByIsNew(true);
    }

    public List<Product> findOnSaleProducts() {
        return productRepository.findByIsOnSale(true);
    }

    public Map<String, List<String>> getAllCategories() {
        List<Product> products = productRepository.findAll();
        Map<String, Set<String>> categoryMap = new HashMap<>();
        
        for (Product product : products) {
            categoryMap.computeIfAbsent(product.getCategory(), k -> new HashSet<>())
                      .add(product.getSubcategory());
        }
        
        return categoryMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new ArrayList<>(e.getValue())
                ));
    }
}