package com.ecommerce.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String brand;
    private double price;
    private double discountPrice;
    private String category;
    private String subcategory;
    private List<String> images;
    private String description;
    private int stock;
    private double rating;
    private int reviewCount;
    private Map<String, String> specs;
    private List<String> features;
    private String slug;
    private boolean isNew;
    private boolean isFeatured;
    private boolean isOnSale;
    private String color;
    private String size;
    private String material;
    private long createdAt;
    private long updatedAt;
    
    // Manual getter methods (Lombok not working properly)
    public String getCategory() {
        return category;
    }
    
    public String getSubcategory() {
        return subcategory;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public double getPrice() {
        return price;
    }
    
    public double getDiscountPrice() {
        return discountPrice;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getStock() {
        return stock;
    }
    
    public double getRating() {
        return rating;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public Map<String, String> getSpecs() {
        return specs;
    }
    
    public List<String> getFeatures() {
        return features;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public boolean isNew() {
        return isNew;
    }
    
    public boolean isFeatured() {
        return isFeatured;
    }
    
    public boolean isOnSale() {
        return isOnSale;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getSize() {
        return size;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
}
