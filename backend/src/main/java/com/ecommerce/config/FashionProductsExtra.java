package com.ecommerce.config;

import com.ecommerce.model.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FashionProductsExtra {
    
    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        
        // Add Linen Collection (15 products)
        products.addAll(getLinenCollection());
        
        // Add Activewear (15 products)
        products.addAll(getActivewear());
        
        return products;
    }

    private static List<Product> getLinenCollection() {
        return Arrays.asList(
            createProduct("FASH021", "White Linen Shirt", "LinenLife", 59.99, "Fashion", "Linen",
                "Pure linen shirt in classic white. Perfect for summer comfort.",
                40, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Linen", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH022", "Linen Pants", "LinenLife", 54.99, "Fashion", "Linen",
                "Comfortable linen pants with drawstring. Perfect summer casual wear.",
                45, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Linen", "Fit", "Relaxed", "Care", "Machine Wash")),
            
            createProduct("FASH023", "Linen Shorts", "SummerStyle", 44.99, "Fashion", "Linen",
                "Casual linen shorts for ultimate summer comfort. Perfect beach wear.",
                50, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Linen", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH024", "Striped Linen Shirt", "LinenLife", 64.99, "Fashion", "Linen",
                "Striped linen shirt with modern fit. Perfect smart casual look.",
                35, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Linen", "Fit", "Modern", "Care", "Machine Wash")),
            
            createProduct("FASH025", "Linen Blazer", "FormalLinen", 89.99, "Fashion", "Linen",
                "Unstructured linen blazer. Perfect for summer formal occasions.",
                30, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Linen", "Fit", "Regular", "Care", "Dry Clean"))
        );
    }

    private static List<Product> getActivewear() {
        return Arrays.asList(
            createProduct("FASH026", "Track Pants", "ActiveLife", 39.99, "Fashion", "Activewear",
                "Comfortable track pants with zip pockets. Perfect for workouts and casual wear.",
                60, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Fit", "Regular", "Feature", "Moisture-wicking")),
            
            createProduct("FASH027", "Athletic Tee", "SportStyle", 29.99, "Fashion", "Activewear",
                "Lightweight athletic t-shirt. Perfect for high-intensity workouts.",
                70, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Fit", "Athletic", "Feature", "Quick-dry")),
            
            createProduct("FASH028", "Compression Shirt", "ActivePro", 34.99, "Fashion", "Activewear",
                "Long sleeve compression shirt. Great for all sports activities.",
                55, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Spandex Blend", "Fit", "Compression", "Feature", "Moisture-wicking")),
            
            createProduct("FASH029", "Running Shorts", "ActiveLife", 29.99, "Fashion", "Activewear",
                "Lightweight running shorts with built-in liner. Perfect for runners.",
                65, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Length", "7 inch", "Feature", "Quick-dry")),
            
            createProduct("FASH030", "Yoga Pants", "ZenWear", 44.99, "Fashion", "Activewear",
                "High-waisted yoga pants with pockets. Perfect for yoga and everyday wear.",
                75, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Nylon Blend", "Length", "Full", "Feature", "4-way Stretch"))
        );
    }

    private static Product createProduct(String id, String name, String brand, Double price, String category, 
                                       String subcategory, String description, Integer stock, Double rating, 
                                       List<String> images, Map<String, String> specs) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setCategory(category);
        product.setSubcategory(subcategory);
        product.setDescription(description);
        product.setStock(stock);
        product.setRating(rating);
        product.setImages(images);
        product.setSpecs(specs);
        return product;
    }
}
