package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
        
        try {
            // Read products from JSON file with GitHub URLs
            System.out.println("Loading products from JSON file with GitHub image URLs...");
            allProducts = loadProductsFromJson();
            System.out.println("Loaded " + allProducts.size() + " products from JSON");
        } catch (Exception e) {
            System.err.println("Failed to load products from JSON, falling back to hardcoded classes: " + e.getMessage());
            
            // Fallback to hardcoded classes if JSON loading fails
            List<Product> electronicsProducts = ElectronicsProducts.getProducts();
            allProducts.addAll(electronicsProducts);
            System.out.println("Added " + electronicsProducts.size() + " electronics products");
            
            List<Product> fashionProducts = FashionProducts.getProducts();
            allProducts.addAll(fashionProducts);
            System.out.println("Added " + fashionProducts.size() + " fashion products");
            
            List<Product> fashionExtraProducts = FashionProductsExtra.getProducts();
            allProducts.addAll(fashionExtraProducts);
            System.out.println("Added " + fashionExtraProducts.size() + " extra fashion products");
            
            List<Product> homeProducts = HomeAndGardenProducts.getProducts();
            allProducts.addAll(homeProducts);
            System.out.println("Added " + homeProducts.size() + " home & garden products");
            
            List<Product> sportsProducts = SportsProducts.getProducts();
            allProducts.addAll(sportsProducts);
            System.out.println("Added " + sportsProducts.size() + " sports products");
        }
        
        // Note: Image URLs are now stored as full GitHub URLs in the product data
        // No need to modify image paths - they come from JSON with complete GitHub URLs
        System.out.println("Using GitHub-hosted image URLs (no path modification needed)...");

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
    
    private List<Product> loadProductsFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        // Try to find the JSON file in the project root
        File jsonFile = new File("ecommerce.products.json");
        if (!jsonFile.exists()) {
            // Try relative to the backend directory
            jsonFile = new File("../ecommerce.products.json");
        }
        if (!jsonFile.exists()) {
            // Try in the current working directory
            jsonFile = new File(System.getProperty("user.dir") + "/ecommerce.products.json");
        }
        
        if (!jsonFile.exists()) {
            throw new IOException("ecommerce.products.json file not found. Please ensure the file exists in the project root.");
        }
        
        System.out.println("Loading products from: " + jsonFile.getAbsolutePath());
        
        try (FileReader reader = new FileReader(jsonFile)) {
            List<Product> products = mapper.readValue(reader, new TypeReference<List<Product>>() {});
            System.out.println("Successfully loaded " + products.size() + " products from JSON");
            
            // Log first few products to verify GitHub URLs
            for (int i = 0; i < Math.min(3, products.size()); i++) {
                Product product = products.get(i);
                System.out.println("Sample product " + (i + 1) + ": " + product.getName());
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    System.out.println("  First image URL: " + product.getImages().get(0));
                }
            }
            
            return products;
        }
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