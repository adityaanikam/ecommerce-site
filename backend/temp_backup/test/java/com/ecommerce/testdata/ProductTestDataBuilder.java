package com.ecommerce.testdata;

import com.ecommerce.model.Product;
import com.ecommerce.model.Product.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProductTestDataBuilder {
    private String id;
    private String name = "Test Product";
    private String description = "Test product description";
    private BigDecimal price = BigDecimal.valueOf(99.99);
    private BigDecimal discountPrice;
    private String category = "Electronics";
    private String subcategory = "Smartphones";
    private String brand = "TestBrand";
    private List<String> images = List.of("image1.jpg", "image2.jpg");
    private List<Specification> specifications = List.of();
    private Integer stock = 100;
    private Product.Ratings ratings;
    private List<Product.Review> reviews = List.of();
    private Boolean isActive = true;
    private String sellerId = "seller123";
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static ProductTestDataBuilder aProduct() {
        return new ProductTestDataBuilder();
    }

    public ProductTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ProductTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ProductTestDataBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ProductTestDataBuilder withDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
        return this;
    }

    public ProductTestDataBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public ProductTestDataBuilder withSubcategory(String subcategory) {
        this.subcategory = subcategory;
        return this;
    }

    public ProductTestDataBuilder withBrand(String brand) {
        this.brand = brand;
        return this;
    }

    public ProductTestDataBuilder withImages(List<String> images) {
        this.images = images;
        return this;
    }

    public ProductTestDataBuilder withSpecifications(List<Specification> specifications) {
        this.specifications = specifications;
        return this;
    }

    public ProductTestDataBuilder withStock(Integer stock) {
        this.stock = stock;
        return this;
    }

    public ProductTestDataBuilder withRatings(Product.Ratings ratings) {
        this.ratings = ratings;
        return this;
    }

    public ProductTestDataBuilder withSellerId(String sellerId) {
        this.sellerId = sellerId;
        return this;
    }

    public ProductTestDataBuilder outOfStock() {
        this.stock = 0;
        return this;
    }

    public ProductTestDataBuilder inactive() {
        this.isActive = false;
        return this;
    }

    public ProductTestDataBuilder withDiscount(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
        return this;
    }

    public Product build() {
        return Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .discountPrice(discountPrice)
                .category(category)
                .subcategory(subcategory)
                .brand(brand)
                .images(images)
                .specifications(specifications)
                .stock(stock)
                .ratings(ratings)
                .reviews(reviews)
                .isActive(isActive)
                .sellerId(sellerId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
