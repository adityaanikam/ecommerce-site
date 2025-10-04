package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer is running...");
        System.out.println("Current product count: " + productRepository.count());
        
        if (productRepository.count() == 0) {
            System.out.println("Database is empty, seeding products...");
            seedProducts();
        } else {
            System.out.println("Database already has products, clearing and reseeding with new image URLs...");
            productRepository.deleteAll();
            seedProducts();
        }
    }

    private void seedProducts() {
        List<Product> allProducts = new ArrayList<>();
        
        // Add Electronics Products (50 products)
        List<Product> electronicsProducts = ElectronicsProducts.getProducts();
        allProducts.addAll(electronicsProducts);
        System.out.println("Added " + electronicsProducts.size() + " electronics products");
        
        // Add Fashion Products (35 products)
        List<Product> fashionProducts = FashionProducts.getProducts();
        allProducts.addAll(fashionProducts);
        System.out.println("Added " + fashionProducts.size() + " fashion products");
        
        // Add Extra Fashion Products (15 products)
        List<Product> fashionExtraProducts = FashionProductsExtra.getProducts();
        allProducts.addAll(fashionExtraProducts);
        System.out.println("Added " + fashionExtraProducts.size() + " extra fashion products");
        
        // Add Home & Garden Products (25 products)
        List<Product> homeProducts = HomeAndGardenProducts.getProducts();
        allProducts.addAll(homeProducts);
        System.out.println("Added " + homeProducts.size() + " home & garden products");
        
        // Add Sports Products (25 products)
        List<Product> sportsProducts = SportsProducts.getProducts();
        allProducts.addAll(sportsProducts);
        System.out.println("Added " + sportsProducts.size() + " sports products");
        
        // Fix image paths for all products
        System.out.println("Fixing image paths for all products...");
        for (Product product : allProducts) {
            String category = product.getCategory();
            String name = product.getName();
            
            // Generate correct image paths based on actual files
            List<String> imagePaths = new ArrayList<>();
            
            // Use correct extensions based on actual files
            String[] extensions;
            if ("Electronics".equals(category)) {
                // Check specific products that use webp
                if (name.contains("AirPods") || name.contains("iPhone 13") || name.contains("iPhone 14") || 
                    name.contains("Bluetooth Speaker") || name.contains("MagSafe") || name.contains("OnePlus") ||
                    name.contains("Oppo") || name.contains("Poco") || name.contains("RAVPower")) {
                    extensions = new String[]{"webp", "webp", "webp"};
                } else {
                    extensions = new String[]{"jpg", "jpg", "jpg"};
                }
            } else if ("Fashion".equals(category)) {
                // Check specific products that use webp
                if (name.contains("Black Crew Neck") || name.contains("Black Jeans") || name.contains("Blue Dress Shirt") ||
                    name.contains("Canvas Belt") || name.contains("Cargo Shorts") || name.contains("Checkered Shirt") ||
                    name.contains("Compression Shirt") || name.contains("Dress Pants") || name.contains("Gray Tee") ||
                    name.contains("Khaki Chinos") || name.contains("Leather Belt") || name.contains("Linen Blazer") ||
                    name.contains("Linen Pants") || name.contains("Linen Shorts") || name.contains("Navy Tee") ||
                    name.contains("Oxford Shirt") || name.contains("Regular Fit Jeans") || name.contains("Running Shorts") ||
                    name.contains("Slim Fit Jeans") || name.contains("Striped Linen Shirt") || name.contains("Striped Tee") ||
                    name.contains("Track Pants") || name.contains("White Dress Shirt") || name.contains("White Linen Shirt") ||
                    name.contains("Yoga Pants")) {
                    extensions = new String[]{"webp", "webp", "webp"};
                } else {
                    extensions = new String[]{"jpg", "jpg", "jpg"};
                }
            } else {
                extensions = new String[]{"jpg", "jpg", "jpg"}; // Default to jpg
            }
            
            for (int i = 0; i < 3; i++) {
                imagePaths.add("/products/" + category + "/" + name + "/" + (i + 1) + "." + extensions[i]);
            }
            
            // Update product with correct image paths
            product.setImages(imagePaths);
        }

        // Set featured, new, and sale flags
        Random random = new Random();
        long now = System.currentTimeMillis();
        
        for (Product product : allProducts) {
            // Set 20% of products as featured
            product.setFeatured(random.nextDouble() < 0.2);
            
            // Set products added in the last 30 days as new
            product.setNew(now - product.getCreatedAt() < 30 * 24 * 60 * 60 * 1000);
            
            // Set random discount for 30% of products (30-70% off)
            if (random.nextDouble() < 0.3) {
                double discountPercentage = 30 + random.nextDouble() * 40; // 30-70% discount
                double discountAmount = product.getPrice() * (discountPercentage / 100.0);
                product.setDiscountPrice(product.getPrice() - discountAmount);
                product.setOnSale(true);
            } else {
                product.setDiscountPrice(0.0);
                product.setOnSale(false);
            }
            
            // Set timestamps if not already set
            if (product.getCreatedAt() == 0) {
                product.setCreatedAt(now - random.nextInt(60) * 24 * 60 * 60 * 1000L); // Random date in last 60 days
            }
            if (product.getUpdatedAt() == 0) {
                product.setUpdatedAt(product.getCreatedAt());
            }
            
            // Set slug if not already set
            if (product.getSlug() == null || product.getSlug().isEmpty()) {
                product.setSlug(product.getId().toLowerCase());
            }
        }

        // Save all products to database
        productRepository.saveAll(allProducts);
        System.out.println("Successfully seeded " + allProducts.size() + " total products!");
        
        // Print category counts
        long electronicsCount = allProducts.stream().filter(p -> p.getCategory().equals("Electronics")).count();
        long fashionCount = allProducts.stream().filter(p -> p.getCategory().equals("Fashion")).count();
        long homeCount = allProducts.stream().filter(p -> p.getCategory().equals("Home & Garden")).count();
        long sportsCount = allProducts.stream().filter(p -> p.getCategory().equals("Sports")).count();
        
        System.out.println("\nCategory Counts:");
        System.out.println("Electronics: " + electronicsCount);
        System.out.println("Fashion: " + fashionCount);
        System.out.println("Home & Garden: " + homeCount);
        System.out.println("Sports: " + sportsCount);
    }
    
    private void updateProductDiscounts() {
        List<Product> allProducts = productRepository.findAll();
        Random random = new Random();
        
        System.out.println("Updating discounts for " + allProducts.size() + " products...");
        
        for (Product product : allProducts) {
            // Set random discount for 30% of products (30-70% off)
            if (random.nextDouble() < 0.3) {
                double discountPercentage = 30 + random.nextDouble() * 40; // 30-70% discount
                double discountAmount = product.getPrice() * (discountPercentage / 100.0);
                product.setDiscountPrice(product.getPrice() - discountAmount);
                product.setOnSale(true);
            } else {
                product.setDiscountPrice(0.0);
                product.setOnSale(false);
            }
        }
        
        productRepository.saveAll(allProducts);
        System.out.println("Successfully updated discounts for all products!");
    }
}