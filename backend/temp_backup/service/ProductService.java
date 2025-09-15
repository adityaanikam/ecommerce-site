package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.Product;
import com.ecommerce.model.Category;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductRepositoryCustom productRepositoryCustom;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;
    private final FileStorageService fileStorageService;
    
    // CRUD Operations
    
    public ProductDto.ProductResponse createProduct(ProductDto.CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        
        // Validate subcategory if provided
        if (request.getSubcategoryId() != null) {
            Category subcategory = categoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found with ID: " + request.getSubcategoryId()));
            
            // Validate subcategory belongs to category
            if (!subcategory.getParentCategory().equals(category.getId())) {
                throw new ValidationException("Subcategory does not belong to the specified category");
            }
        }
        
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .category(category.getId())
                .subcategory(request.getSubcategoryId())
                .brand(request.getBrand())
                .images(request.getImages())
                .specifications(request.getSpecifications())
                .stock(request.getStock())
                .sellerId(request.getSellerId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        
        // Clear cache
        cacheService.evictProductCache(savedProduct.getId());
        cacheService.evictCategoryCache(category.getId());
        
        return mapToProductResponse(savedProduct);
    }
    
    public ProductDto.ProductResponse getProductById(String id) {
        log.debug("Fetching product by ID: {}", id);
        
        // Try cache first
        ProductDto.ProductResponse cached = cacheService.getProductFromCache(id);
        if (cached != null) {
            return cached;
        }
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        ProductDto.ProductResponse response = mapToProductResponse(product);
        
        // Cache the result
        cacheService.cacheProduct(id, response);
        
        return response;
    }
    
    public Page<ProductDto.ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination");
        
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public ProductDto.ProductResponse updateProduct(String id, ProductDto.UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Validate category if changed
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        }
        
        // Update fields
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) product.setDiscountPrice(request.getDiscountPrice());
        if (request.getCategoryId() != null) product.setCategory(request.getCategoryId());
        if (request.getSubcategoryId() != null) product.setSubcategory(request.getSubcategoryId());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getImages() != null) product.setImages(request.getImages());
        if (request.getSpecifications() != null) product.setSpecifications(request.getSpecifications());
        if (request.getStock() != null) product.setStock(request.getStock());
        
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", savedProduct.getId());
        
        // Clear cache
        cacheService.evictProductCache(id);
        cacheService.evictCategoryCache(product.getCategory());
        
        return mapToProductResponse(savedProduct);
    }
    
    public void deleteProduct(String id) {
        log.info("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Soft delete
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        
        // Clear cache
        cacheService.evictProductCache(id);
        cacheService.evictCategoryCache(product.getCategory());
        
        log.info("Product deleted successfully with ID: {}", id);
    }
    
    // Search and Filter Operations
    
    public Page<ProductDto.ProductResponse> searchProducts(String query, Pageable pageable) {
        log.debug("Searching products with query: {}", query);
        
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query, pageable);
        
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductDto.ProductResponse> getProductsByCategory(String categoryId, Pageable pageable) {
        log.debug("Fetching products by category ID: {}", categoryId);
        
        Page<Product> products = productRepository.findByCategoryAndIsActiveTrue(categoryId, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductDto.ProductResponse> getProductsByBrand(String brand, Pageable pageable) {
        log.debug("Fetching products by brand: {}", brand);
        
        Page<Product> products = productRepository.findByBrandAndIsActiveTrue(brand, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductDto.ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        log.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);
        
        Page<Product> products = productRepository.findByPriceBetweenAndIsActiveTrue(minPrice, maxPrice, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public List<ProductDto.ProductResponse> getFeaturedProducts(int limit) {
        log.debug("Fetching featured products with limit: {}", limit);
        
        List<Product> products = productRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    // Advanced Search with Filters
    
    public Page<ProductDto.ProductResponse> searchProductsWithFilters(ProductDto.ProductSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching products with advanced filters");
        
        return productRepositoryCustom.searchProductsWithFilters(searchRequest, pageable);
    }
    
    // Inventory Management
    
    public ProductDto.ProductResponse updateStock(String productId, Integer newStock) {
        log.info("Updating stock for product ID: {} to {}", productId, newStock);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (newStock < 0) {
            throw new ValidationException("Stock cannot be negative");
        }
        
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        
        // Clear cache
        cacheService.evictProductCache(productId);
        
        log.info("Stock updated successfully for product ID: {}", productId);
        return mapToProductResponse(savedProduct);
    }
    
    public ProductDto.ProductResponse reduceStock(String productId, Integer quantity) {
        log.info("Reducing stock for product ID: {} by {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        
        if (product.getStock() < quantity) {
            throw new ValidationException("Insufficient stock. Available: " + product.getStock() + ", Requested: " + quantity);
        }
        
        product.setStock(product.getStock() - quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        
        // Clear cache
        cacheService.evictProductCache(productId);
        
        log.info("Stock reduced successfully for product ID: {}", productId);
        return mapToProductResponse(savedProduct);
    }
    
    public List<ProductDto.ProductResponse> getLowStockProducts(Integer threshold) {
        log.debug("Fetching products with stock below threshold: {}", threshold);
        
        List<Product> products = productRepository.findByStockLessThanAndIsActiveTrue(threshold);
        
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    // Image Upload Handling
    
    public ProductDto.ProductResponse uploadProductImages(String productId, List<MultipartFile> images) {
        log.info("Uploading images for product ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        List<String> imageUrls = fileStorageService.storeProductImages(images);
        
        // Add new images to existing ones
        product.getImages().addAll(imageUrls);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        
        // Clear cache
        cacheService.evictProductCache(productId);
        
        log.info("Images uploaded successfully for product ID: {}", productId);
        return mapToProductResponse(savedProduct);
    }
    
    // Analytics and Reporting
    
    public Map<String, Object> getProductAnalytics(String productId) {
        log.debug("Getting analytics for product ID: {}", productId);
        
        return productRepositoryCustom.getProductAnalytics(productId);
    }
    
    // Utility Methods
    
    private ProductDto.ProductResponse mapToProductResponse(Product product) {
        return ProductDto.ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .category(product.getCategory())
                .subcategory(product.getSubcategory())
                .brand(product.getBrand())
                .images(product.getImages())
                .specifications(product.getSpecifications())
                .stock(product.getStock())
                .ratings(product.getRatings())
                .reviews(product.getReviews())
                .sellerId(product.getSellerId())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    public boolean isProductAvailable(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        return product.getIsActive() && product.getStock() >= quantity;
    }
    
    public Double getProductPrice(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        return product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
    }
}