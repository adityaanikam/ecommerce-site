package com.ecommerce.config;

import com.ecommerce.model.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SportsProducts {
    
    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        
        // Gym Equipment (10 products)
        products.addAll(Arrays.asList(
            createProduct("SPORT001", "Adjustable Dumbbells", "FitPro", 299.99, "Sports", "Gym Equipment",
                "Space-saving adjustable dumbbells from 5-52.5 lbs. Perfect for home gym.",
                25, 4.8, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Weight Range", "5-52.5 lbs", "Adjustments", "15 settings", "Material", "Steel")),
            
            createProduct("SPORT002", "Resistance Bands Set", "FitPro", 29.99, "Sports", "Gym Equipment",
                "Set of 5 resistance bands with different resistance levels. Perfect for strength training and rehabilitation.",
                100, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Pieces", "5", "Resistance", "10-50 lbs", "Material", "Natural Latex")),
            
            createProduct("SPORT003", "Kettlebell 20lb", "IronFit", 39.99, "Sports", "Gym Equipment",
                "Cast iron kettlebell with comfortable grip. Perfect for full-body workouts.",
                40, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Weight", "20 lbs", "Material", "Cast Iron", "Handle", "Textured")),
            
            createProduct("SPORT004", "Pull-up Bar", "HomeGym", 34.99, "Sports", "Gym Equipment",
                "Doorway pull-up bar with multiple grip positions. Easy installation and removal.",
                50, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Max Weight", "300 lbs", "Width", "24-36 inches", "Material", "Steel")),
            
            createProduct("SPORT005", "Ab Wheel", "CoreFit", 19.99, "Sports", "Gym Equipment",
                "Dual-wheel ab roller with comfortable grips. Great for core strengthening.",
                60, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Wheel Type", "Dual", "Material", "PVC", "Grip", "Non-slip"))
        ));

        // Yoga & Fitness (5 products)
        products.addAll(Arrays.asList(
            createProduct("SPORT006", "Yoga Mat", "YogaPro", 29.99, "Sports", "Yoga",
                "Premium yoga mat with alignment lines. Non-slip and eco-friendly material.",
                80, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Thickness", "6mm", "Material", "TPE", "Size", "72x24 inches")),
            
            createProduct("SPORT007", "Yoga Blocks", "YogaPro", 19.99, "Sports", "Yoga",
                "Set of 2 high-density foam yoga blocks. Perfect for support and stability.",
                70, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "EVA Foam", "Size", "9x6x4 inches", "Pieces", "2")),
            
            createProduct("SPORT008", "Yoga Strap", "YogaPro", 9.99, "Sports", "Yoga",
                "Cotton yoga strap with D-ring buckle. Great for stretching and flexibility.",
                90, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Length", "8 feet", "Width", "1.5 inches")),
            
            createProduct("SPORT009", "Balance Ball", "FitPro", 24.99, "Sports", "Yoga",
                "Anti-burst exercise ball with pump. Perfect for yoga and core training.",
                55, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "65cm", "Material", "PVC", "Max Weight", "2000 lbs")),
            
            createProduct("SPORT010", "Meditation Cushion", "ZenFit", 39.99, "Sports", "Yoga",
                "Comfortable meditation cushion with carry handle. Perfect height for proper posture.",
                40, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Fill", "Buckwheat", "Size", "13x6 inches"))
        ));

        // Fitness Apparel (5 products)
        products.addAll(Arrays.asList(
            createProduct("SPORT011", "Athletic Shirt", "SportWear", 29.99, "Sports", "Apparel",
                "Moisture-wicking athletic t-shirt. Perfect for workouts and running.",
                75, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Fit", "Athletic", "Feature", "Moisture-wicking")),
            
            createProduct("SPORT012", "Gym Shorts", "SportWear", 24.99, "Sports", "Apparel",
                "Quick-dry gym shorts with pockets. Comfortable and functional.",
                70, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Length", "7 inch", "Feature", "Quick-dry")),
            
            createProduct("SPORT013", "Compression Leggings", "FitWear", 34.99, "Sports", "Apparel",
                "High-waisted compression leggings. Perfect for all types of workouts.",
                65, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Spandex Blend", "Length", "Full", "Feature", "Compression")),
            
            createProduct("SPORT014", "Sports Bra", "FitWear", 29.99, "Sports", "Apparel",
                "Medium support sports bra. Comfortable and moisture-wicking.",
                60, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Nylon Blend", "Support", "Medium", "Feature", "Moisture-wicking")),
            
            createProduct("SPORT015", "Tank Top", "SportWear", 19.99, "Sports", "Apparel",
                "Breathable athletic tank top. Perfect for high-intensity workouts.",
                80, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Fit", "Regular", "Feature", "Breathable"))
        ));

        // Accessories (5 products)
        products.addAll(Arrays.asList(
            createProduct("SPORT016", "Water Bottle", "HydroFit", 19.99, "Sports", "Accessories",
                "BPA-free water bottle with time markers. Helps track daily water intake.",
                100, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Capacity", "32 oz", "Material", "Tritan", "Feature", "Time Markers")),
            
            createProduct("SPORT017", "Gym Bag", "SportGear", 39.99, "Sports", "Accessories",
                "Spacious gym bag with shoe compartment. Perfect for gym and travel.",
                45, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Size", "Medium", "Feature", "Shoe Compartment")),
            
            createProduct("SPORT018", "Gym Towel", "FitStyle", 14.99, "Sports", "Accessories",
                "Quick-dry microfiber gym towel. Perfect size for workouts.",
                120, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Microfiber", "Size", "16x32 inches", "Feature", "Quick-dry")),
            
            createProduct("SPORT019", "Lifting Gloves", "GripPro", 19.99, "Sports", "Accessories",
                "Padded weight lifting gloves. Provides grip and hand protection.",
                85, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Leather", "Size", "M/L/XL", "Feature", "Padded")),
            
            createProduct("SPORT020", "Knee Sleeves", "JointPro", 24.99, "Sports", "Accessories",
                "Compression knee sleeves for support. Perfect for squats and running.",
                70, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Neoprene", "Size", "M/L/XL", "Feature", "Compression")),
            
            // Recovery Products (5 products)
            createProduct("SPORT021", "Foam Roller", "RecoveryPro", 29.99, "Sports", "Recovery",
                "High-density foam roller for muscle recovery. Perfect for post-workout.",
                45, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Length", "18 inch", "Density", "High", "Type", "Textured")),
            
            createProduct("SPORT022", "Massage Ball", "RecoveryPro", 14.99, "Sports", "Recovery",
                "Deep tissue massage ball. Perfect for targeting specific muscle groups.",
                60, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "2.5 inch", "Material", "Rubber", "Type", "Firm")),
            
            createProduct("SPORT023", "Compression Socks", "RecoveryWear", 19.99, "Sports", "Recovery",
                "Graduated compression socks for improved circulation. Perfect for recovery and travel.",
                80, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Nylon Blend", "Compression", "20-30mmHg", "Length", "Knee High")),
            
            createProduct("SPORT024", "Ice Pack", "CoolRelief", 12.99, "Sports", "Recovery",
                "Reusable ice pack with compression wrap. Perfect for injury recovery.",
                90, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "10x12 inch", "Type", "Gel", "Feature", "Flexible")),
            
            createProduct("SPORT025", "Recovery Bands", "FlexFit", 16.99, "Sports", "Recovery",
                "Light resistance bands for recovery exercises. Perfect for rehabilitation.",
                75, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Resistance", "Light", "Material", "Latex", "Pack", "3"))
        ));

        return products;
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
