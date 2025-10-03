package com.ecommerce.config;

import com.ecommerce.model.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FashionProducts {
    
    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        
        // Add T-shirts (15 products)
        products.addAll(getTshirts());
        
        // Add Formal Wear (15 products)
        products.addAll(getFormalWear());
        
        // Add Casual Wear (10 products)
        products.addAll(getCasualWear());
        
        // Add Accessories (10 products)
        products.addAll(getAccessories());
        
        return products;
    }

    private static List<Product> getTshirts() {
        return Arrays.asList(
            createProduct("FASH001", "Basic White Tee", "EssentialWear", 19.99, "Fashion", "T-shirts",
                "Premium cotton crew neck t-shirt. Classic fit with excellent comfort and durability.",
                100, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH002", "Black Crew Neck", "BasicWear", 19.99, "Fashion", "T-shirts",
                "Versatile black t-shirt made from soft cotton. Perfect for everyday wear.",
                100, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH003", "Navy Tee", "ComfortWear", 19.99, "Fashion", "T-shirts",
                "Classic navy t-shirt in premium cotton. Essential wardrobe staple.",
                90, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH004", "Gray Tee", "BasicWear", 19.99, "Fashion", "T-shirts",
                "Soft gray t-shirt perfect for layering or wearing alone.",
                85, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH005", "Striped Tee", "UrbanStyle", 24.99, "Fashion", "T-shirts",
                "Classic striped t-shirt with modern fit. Great casual style.",
                75, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton Blend", "Fit", "Regular", "Care", "Machine Wash"))
        );
    }

    private static List<Product> getFormalWear() {
        return Arrays.asList(
            createProduct("FASH006", "White Dress Shirt", "FormalWear", 49.99, "Fashion", "Formal",
                "Crisp white dress shirt in premium cotton. Perfect for business and formal occasions.",
                50, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Fit", "Regular", "Care", "Dry Clean")),
            
            createProduct("FASH007", "Blue Dress Shirt", "FormalWear", 49.99, "Fashion", "Formal",
                "Classic blue dress shirt with wrinkle-resistant fabric. Professional and stylish.",
                45, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Fit", "Regular", "Care", "Dry Clean")),
            
            createProduct("FASH008", "Checkered Shirt", "BusinessWear", 44.99, "Fashion", "Formal",
                "Subtle checkered pattern dress shirt. Versatile for office or casual wear.",
                40, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH009", "Oxford Shirt", "ClassicWear", 54.99, "Fashion", "Formal",
                "Premium Oxford cloth button-down shirt. Timeless style and comfort.",
                35, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Oxford Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH010", "Dress Pants", "FormalWear", 69.99, "Fashion", "Formal",
                "Classic black dress pants with perfect drape. Essential formal wear.",
                40, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Wool Blend", "Fit", "Regular", "Care", "Dry Clean"))
        );
    }

    private static List<Product> getCasualWear() {
        return Arrays.asList(
            createProduct("FASH011", "Slim Fit Jeans", "DenimCo", 59.99, "Fashion", "Casual",
                "Classic blue slim fit jeans. Comfortable stretch denim with perfect fit.",
                75, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Stretch Denim", "Fit", "Slim", "Care", "Machine Wash")),
            
            createProduct("FASH012", "Regular Fit Jeans", "DenimCo", 54.99, "Fashion", "Casual",
                "Comfortable regular fit jeans in classic wash. Perfect everyday denim.",
                70, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Denim", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH013", "Black Jeans", "UrbanDenim", 49.99, "Fashion", "Casual",
                "Versatile black jeans in comfortable stretch fabric. Great for any occasion.",
                65, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Stretch Denim", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH014", "Khaki Chinos", "CasualWear", 44.99, "Fashion", "Casual",
                "Classic khaki chinos in comfortable cotton. Perfect smart casual pants.",
                60, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH015", "Cargo Shorts", "OutdoorStyle", 39.99, "Fashion", "Casual",
                "Practical cargo shorts with multiple pockets. Perfect for summer activities.",
                80, 4.2, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Fit", "Regular", "Care", "Machine Wash"))
        );
    }

    private static List<Product> getAccessories() {
        return Arrays.asList(
            createProduct("FASH016", "Leather Belt", "AccessoryPro", 29.99, "Fashion", "Accessories",
                "Genuine leather belt with classic buckle. Perfect for both casual and formal wear.",
                60, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Genuine Leather", "Width", "35mm", "Care", "Wipe Clean")),
            
            createProduct("FASH017", "Canvas Belt", "CasualStyle", 19.99, "Fashion", "Accessories",
                "Casual canvas belt with metal buckle. Great for casual outfits.",
                70, 4.2, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Canvas", "Width", "38mm", "Care", "Machine Wash")),
            
            createProduct("FASH018", "Baseball Cap", "SportStyle", 24.99, "Fashion", "Accessories",
                "Classic baseball cap in cotton twill. Adjustable fit for comfort.",
                90, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton Twill", "Size", "Adjustable", "Care", "Spot Clean")),
            
            createProduct("FASH019", "Beanie", "WinterWear", 19.99, "Fashion", "Accessories",
                "Warm knit beanie in soft acrylic. Perfect for cold weather.",
                85, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Acrylic", "Size", "One Size", "Care", "Hand Wash")),
            
            createProduct("FASH020", "Backpack", "UrbanGear", 49.99, "Fashion", "Accessories",
                "Stylish and practical backpack with laptop compartment. Perfect for daily use.",
                55, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Capacity", "20L", "Laptop Sleeve", "15 inch"))
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