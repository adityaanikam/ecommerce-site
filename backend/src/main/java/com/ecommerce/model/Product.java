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
    
    // Manual setter methods (Lombok not working properly)
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public void setFeatured(boolean featured) {
        this.isFeatured = featured;
    }
    
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
    
    public void setOnSale(boolean onSale) {
        this.isOnSale = onSale;
    }
    
    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public void setSpecs(Map<String, String> specs) {
        this.specs = specs;
    }
    
    public void setFeatures(List<String> features) {
        this.features = features;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
}
