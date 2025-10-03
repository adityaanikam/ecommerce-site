package com.ecommerce.config;

import com.ecommerce.model.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HomeAndGardenProducts {
    
    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        
        // Curtains (6 products)
        products.addAll(Arrays.asList(
            createProduct("HOME001", "Blackout Curtains", "HomeStyle", 49.99, "Home & Garden", "Curtains",
                "100% blackout curtains with thermal insulation. Perfect for bedroom and living room.",
                40, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Size", "52x84 inches", "Features", "Blackout")),
            
            createProduct("HOME002", "Sheer Curtains", "LightStyle", 29.99, "Home & Garden", "Curtains",
                "Light and airy sheer curtains. Perfect for letting in natural light while maintaining privacy.",
                50, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Voile", "Size", "52x84 inches", "Features", "Sheer")),
            
            createProduct("HOME003", "Thermal Curtains", "ComfortHome", 59.99, "Home & Garden", "Curtains",
                "Energy-efficient thermal curtains. Help maintain room temperature and reduce energy costs.",
                35, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Thermal Fabric", "Size", "52x84 inches", "Features", "Insulating")),
            
            createProduct("HOME004", "Room Divider", "SpaceStyle", 79.99, "Home & Garden", "Curtains",
                "Elegant room divider curtain. Perfect for creating separate spaces in open areas.",
                30, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Heavy Fabric", "Size", "8x8 feet", "Features", "Room Division")),
            
            createProduct("HOME005", "Window Valance", "ElegantHome", 24.99, "Home & Garden", "Curtains",
                "Decorative window valance. Adds style and elegance to any window treatment.",
                45, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Polyester", "Size", "52x18 inches", "Style", "Traditional")),
            
            createProduct("HOME006", "Curtain Rods", "HomeHardware", 34.99, "Home & Garden", "Curtains",
                "Adjustable curtain rods with decorative finials. Easy installation and sturdy construction.",
                60, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Metal", "Length", "28-48 inches", "Finish", "Bronze"))
        ));

        // Plants (7 products)
        products.addAll(Arrays.asList(
            createProduct("HOME007", "Monstera Deliciosa", "GreenThumb", 39.99, "Home & Garden", "Plants",
                "Popular indoor plant with unique split leaves. Easy to care for and air-purifying.",
                30, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Medium", "Light", "Indirect", "Care", "Weekly Water")),
            
            createProduct("HOME008", "Snake Plant", "PlantLife", 29.99, "Home & Garden", "Plants",
                "Low-maintenance indoor plant. Perfect for air purification and modern decor.",
                40, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Medium", "Light", "Low to Bright", "Care", "Monthly Water")),
            
            createProduct("HOME009", "Peace Lily", "GreenHome", 24.99, "Home & Garden", "Plants",
                "Beautiful flowering plant that helps clean indoor air. Easy to care for.",
                35, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Small", "Light", "Low to Medium", "Care", "Weekly Water")),
            
            createProduct("HOME010", "Pothos", "PlantLife", 19.99, "Home & Garden", "Plants",
                "Trailing vine plant perfect for hanging baskets. Very easy to grow.",
                45, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Small", "Light", "Low to Bright", "Care", "Weekly Water")),
            
            createProduct("HOME011", "Rubber Plant", "GreenThumb", 34.99, "Home & Garden", "Plants",
                "Statement plant with large, glossy leaves. Great for adding height to room decor.",
                25, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Large", "Light", "Bright Indirect", "Care", "Weekly Water")),
            
            createProduct("HOME012", "ZZ Plant", "PlantLife", 27.99, "Home & Garden", "Plants",
                "Nearly indestructible plant perfect for beginners. Tolerates low light conditions.",
                35, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Medium", "Light", "Low to Bright", "Care", "Monthly Water")),
            
            createProduct("HOME013", "Spider Plant", "GreenHome", 22.99, "Home & Garden", "Plants",
                "Classic hanging plant that produces babies. Great air purifier.",
                40, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "Medium", "Light", "Indirect", "Care", "Weekly Water"))
        ));

        // Furniture (6 products)
        products.addAll(Arrays.asList(
            createProduct("HOME014", "Coffee Table", "FurnitureCo", 149.99, "Home & Garden", "Furniture",
                "Modern coffee table with storage shelf. Perfect centerpiece for any living room.",
                20, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Wood", "Size", "47x24 inches", "Style", "Modern")),
            
            createProduct("HOME015", "Side Table", "HomeStyle", 79.99, "Home & Garden", "Furniture",
                "Compact side table with drawer. Great for small spaces and storage.",
                25, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Wood", "Size", "20x20 inches", "Style", "Contemporary")),
            
            createProduct("HOME016", "Floor Lamp", "LightingCo", 89.99, "Home & Garden", "Furniture",
                "Modern floor lamp with adjustable head. Perfect for reading and ambient lighting.",
                30, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Metal", "Height", "65 inches", "Bulb Type", "LED")),
            
            createProduct("HOME017", "Table Lamp", "LightStyle", 49.99, "Home & Garden", "Furniture",
                "Stylish table lamp with fabric shade. Great for bedside or desk lighting.",
                35, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Ceramic", "Height", "20 inches", "Shade", "Fabric")),
            
            createProduct("HOME018", "Throw Pillow", "ComfortHome", 24.99, "Home & Garden", "Furniture",
                "Decorative throw pillow with removable cover. Adds comfort and style to any room.",
                50, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Cotton", "Size", "18x18 inches", "Care", "Machine Wash")),
            
            createProduct("HOME019", "Area Rug", "FloorDecor", 129.99, "Home & Garden", "Furniture",
                "Soft area rug with modern pattern. Perfect for defining spaces and adding warmth.",
                15, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Synthetic", "Size", "5x7 feet", "Style", "Contemporary"))
        ));

        // Decor (6 products)
        products.addAll(Arrays.asList(
            createProduct("HOME020", "Wall Clock", "TimeStyle", 39.99, "Home & Garden", "Decor",
                "Modern wall clock with silent mechanism. Perfect blend of function and style.",
                40, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Metal", "Size", "12 inch", "Style", "Modern")),
            
            createProduct("HOME021", "Picture Frame", "HomeDecor", 29.99, "Home & Garden", "Decor",
                "Classic picture frame in brushed metal finish. Perfect for photos and artwork.",
                45, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Metal", "Size", "8x10 inches", "Style", "Classic")),
            
            createProduct("HOME022", "Vase", "ArtDecor", 34.99, "Home & Garden", "Decor",
                "Ceramic vase with modern design. Perfect for fresh or artificial flowers.",
                35, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Ceramic", "Height", "12 inches", "Style", "Modern")),
            
            createProduct("HOME023", "Candle Holder", "LightDecor", 19.99, "Home & Garden", "Decor",
                "Glass candle holder with metallic finish. Creates beautiful ambient lighting.",
                50, 4.2, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Glass", "Size", "4x6 inches", "Style", "Contemporary")),
            
            createProduct("HOME024", "Wall Art", "ArtHome", 79.99, "Home & Garden", "Decor",
                "Abstract wall art print in modern style. Makes a bold statement in any room.",
                25, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Canvas", "Size", "24x36 inches", "Style", "Abstract")),
            
            createProduct("HOME025", "Mirror", "ReflectStyle", 89.99, "Home & Garden", "Decor",
                "Decorative wall mirror with elegant frame. Adds light and space to any room.",
                20, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Glass & Wood", "Size", "30x40 inches", "Style", "Traditional"))
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
