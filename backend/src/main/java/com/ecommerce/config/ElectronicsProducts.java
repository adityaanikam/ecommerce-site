package com.ecommerce.config;

import com.ecommerce.model.Product;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ElectronicsProducts {
    
    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        
        // Mobiles (15 products)
        products.addAll(Arrays.asList(
            createProduct("ELEC001", "iPhone 15 Pro", "Apple", 999.99, "Electronics", "Mobiles",
                "Latest iPhone with titanium design and A17 Pro chip. Features a stunning 48MP camera system and advanced computational photography.",
                50, 4.8, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.1-inch Super Retina XDR", "Processor", "A17 Pro", "Storage", "256GB")),
            
            createProduct("ELEC002", "Samsung Galaxy S24", "Samsung", 899.99, "Electronics", "Mobiles",
                "Premium Android flagship with advanced AI features and a stunning Dynamic AMOLED display. Exceptional camera system and all-day battery life.",
                45, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.8-inch Dynamic AMOLED", "Processor", "Snapdragon 8 Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC003", "iPhone 14", "Apple", 799.99, "Electronics", "Mobiles",
                "Powerful iPhone with A15 Bionic chip and advanced dual-camera system. Features all-day battery life and Ceramic Shield front cover.",
                60, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.1-inch Super Retina XDR", "Processor", "A15 Bionic", "Storage", "128GB")),
            
            createProduct("ELEC004", "iPhone 13", "Apple", 699.99, "Electronics", "Mobiles",
                "Feature-rich iPhone with A15 Bionic chip and impressive camera capabilities. Great value for performance and quality.",
                55, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.1-inch Super Retina XDR", "Processor", "A15 Bionic", "Storage", "128GB")),
            
            createProduct("ELEC005", "Samsung Galaxy A54", "Samsung", 449.99, "Electronics", "Mobiles",
                "Mid-range smartphone with excellent features and great camera performance. Perfect balance of features and value.",
                70, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.4-inch Super AMOLED", "Processor", "Exynos 1380", "Storage", "128GB")),
            
            createProduct("ELEC006", "Xiaomi 14", "Xiaomi", 699.99, "Electronics", "Mobiles",
                "Flagship phone with Leica optics and powerful Snapdragon processor. Premium build quality and excellent performance.",
                40, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.36-inch AMOLED", "Processor", "Snapdragon 8 Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC007", "OnePlus 11", "OnePlus", 699.99, "Electronics", "Mobiles",
                "Flagship killer with Hasselblad cameras and lightning-fast charging. Smooth performance and premium design.",
                45, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.7-inch AMOLED", "Processor", "Snapdragon 8 Gen 2", "Storage", "256GB")),
            
            createProduct("ELEC008", "Realme GT 5", "Realme", 499.99, "Electronics", "Mobiles",
                "Performance-focused phone with gaming features and fast charging. Great value for high-end specs.",
                50, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.74-inch AMOLED", "Processor", "Snapdragon 8 Gen 2", "Storage", "256GB")),
            
            createProduct("ELEC009", "Google Pixel 8", "Google", 699.99, "Electronics", "Mobiles",
                "Pure Android experience with exceptional computational photography. Powered by Google Tensor G3 chip.",
                40, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.2-inch OLED", "Processor", "Google Tensor G3", "Storage", "128GB")),
            
            createProduct("ELEC010", "Honor Magic 6", "Honor", 649.99, "Electronics", "Mobiles",
                "Flagship smartphone with powerful camera system and sleek design. Features advanced AI capabilities.",
                35, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.78-inch OLED", "Processor", "Snapdragon 8 Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC011", "Oppo Reno 11", "Oppo", 359.99, "Electronics", "Mobiles",
                "Stylish mid-range phone with excellent camera capabilities and fast charging. Great value proposition.",
                50, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.7-inch AMOLED", "Processor", "Dimensity 8200", "Storage", "256GB")),
            
            createProduct("ELEC012", "Poco X6", "Xiaomi", 249.99, "Electronics", "Mobiles",
                "Budget gaming phone with high refresh rate display and powerful processor. Excellent performance for the price.",
                65, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.67-inch AMOLED", "Processor", "Snapdragon 7s Gen 2", "Storage", "256GB")),
            
            createProduct("ELEC013", "Redmi Note 13", "Xiaomi", 299.99, "Electronics", "Mobiles",
                "Feature-packed budget phone with great camera and battery life. Perfect for everyday use.",
                80, 4.2, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.67-inch AMOLED", "Processor", "Snapdragon 685", "Storage", "128GB")),
            
            createProduct("ELEC014", "Samsung A55", "Samsung", 399.99, "Electronics", "Mobiles",
                "Mid-range phone with excellent features and build quality. Great all-around performance.",
                70, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.6-inch Super AMOLED", "Processor", "Exynos 1480", "Storage", "128GB")),
            
            createProduct("ELEC015", "Realme GT 6", "Realme", 449.99, "Electronics", "Mobiles",
                "High-performance phone with gaming features and fast charging. Excellent value for money.",
                55, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "6.78-inch AMOLED", "Processor", "Snapdragon 8s Gen 3", "Storage", "256GB"))
        ));

        // Tablets and Laptops (15 products)
        products.addAll(Arrays.asList(
            createProduct("ELEC016", "iPad Pro 11\"", "Apple", 799.99, "Electronics", "Tablets",
                "Powerful tablet with M2 chip and stunning Liquid Retina display. Perfect for creativity and productivity.",
                30, 4.8, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "11-inch Liquid Retina", "Processor", "M2", "Storage", "256GB")),
            
            createProduct("ELEC017", "Surface Laptop 5", "Microsoft", 999.99, "Electronics", "Laptops",
                "Premium laptop with touchscreen and all-day battery life. Perfect blend of style and performance.",
                25, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "13.5-inch PixelSense", "Processor", "Intel Core i5", "Storage", "512GB")),
            
            createProduct("ELEC018", "MacBook Air M2", "Apple", 1199.99, "Electronics", "Laptops",
                "Ultra-thin laptop with powerful M2 chip and stunning Retina display. Amazing performance and battery life.",
                20, 4.9, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "13.6-inch Liquid Retina", "Processor", "M2", "Storage", "512GB")),
            
            createProduct("ELEC019", "Dell XPS 13", "Dell", 1299.99, "Electronics", "Laptops",
                "Premium ultrabook with InfinityEdge display and powerful performance. Perfect for professionals.",
                15, 4.8, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "13.4-inch InfinityEdge", "Processor", "Intel Core i7", "Storage", "1TB")),
            
            createProduct("ELEC020", "HP Pavilion", "HP", 699.99, "Electronics", "Laptops",
                "Versatile laptop for everyday computing needs. Great performance at an affordable price.",
                30, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Display", "15.6-inch FHD", "Processor", "AMD Ryzen 5", "Storage", "512GB"))
        ));

        // Audio Products (10 products)
        products.addAll(Arrays.asList(
            createProduct("ELEC021", "Sony WH-1000XM5", "Sony", 399.99, "Electronics", "Audio",
                "Industry-leading noise cancelling headphones with exceptional sound quality. Premium comfort and features.",
                40, 4.8, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "Over-ear", "Battery Life", "30 hours", "Noise Cancelling", "Yes")),
            
            createProduct("ELEC022", "AirPods Pro 2", "Apple", 249.99, "Electronics", "Audio",
                "Premium wireless earbuds with active noise cancellation and spatial audio. Amazing sound quality and comfort.",
                50, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "In-ear", "Battery Life", "6 hours", "Noise Cancelling", "Yes")),
            
            createProduct("ELEC023", "Sony WH-CH720N", "Sony", 149.99, "Electronics", "Audio",
                "Mid-range noise cancelling headphones with great sound quality. Excellent value for money.",
                60, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "Over-ear", "Battery Life", "35 hours", "Noise Cancelling", "Yes")),
            
            createProduct("ELEC024", "JBL Tune 760NC", "JBL", 129.99, "Electronics", "Audio",
                "Wireless headphones with active noise cancellation and JBL Pure Bass sound.",
                70, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "Over-ear", "Battery Life", "35 hours", "Noise Cancelling", "Yes")),
            
            createProduct("ELEC025", "Beats Solo 3", "Beats", 199.99, "Electronics", "Audio",
                "Wireless on-ear headphones with Apple W1 chip. Great sound and seamless connectivity.",
                45, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "On-ear", "Battery Life", "40 hours", "Noise Cancelling", "No"))
        ));

        // Accessories (20 products)
        products.addAll(Arrays.asList(
            createProduct("ELEC026", "Anker PowerCore 26800", "Anker", 59.99, "Electronics", "Accessories",
                "High-capacity power bank with fast charging support. Perfect for multiple device charging.",
                100, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Capacity", "26800mAh", "Ports", "3 USB-A", "Fast Charging", "Yes")),
            
            createProduct("ELEC027", "RAVPower 20000mAh", "RAVPower", 49.99, "Electronics", "Accessories",
                "Compact power bank with PD fast charging. Great for travel and everyday use.",
                80, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Capacity", "20000mAh", "Ports", "USB-C PD", "Fast Charging", "Yes")),
            
            createProduct("ELEC028", "Baseus Power Bank", "Baseus", 39.99, "Electronics", "Accessories",
                "Slim power bank with wireless charging capability. Perfect for modern devices.",
                90, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Capacity", "10000mAh", "Wireless", "Yes", "Fast Charging", "Yes")),
            
            createProduct("ELEC029", "iPhone 15 Case", "Apple", 49.99, "Electronics", "Accessories",
                "Official silicone case with MagSafe support. Perfect protection for your iPhone.",
                120, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Silicone", "MagSafe", "Yes", "Color", "Various")),
            
            createProduct("ELEC030", "Samsung S24 Screen Protector", "Spigen", 14.99, "Electronics", "Accessories",
                "Tempered glass screen protector with easy installation. Maximum protection for your screen.",
                150, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Tempered Glass", "Hardness", "9H", "Anti-Fingerprint", "Yes")),
            
            createProduct("ELEC031", "USB-C Hub", "Anker", 39.99, "Electronics", "Accessories",
                "7-in-1 USB-C hub with HDMI, USB ports, and card reader. Perfect for laptops.",
                60, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Ports", "7", "HDMI", "4K@60Hz", "Power Delivery", "100W")),
            
            createProduct("ELEC032", "Lightning Cable", "Apple", 19.99, "Electronics", "Accessories",
                "Official MFi-certified Lightning to USB-C cable. Fast charging support.",
                100, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Length", "1m", "Type", "USB-C to Lightning", "MFi", "Yes")),
            
            createProduct("ELEC033", "MagSafe Charger", "Apple", 39.99, "Electronics", "Accessories",
                "Official MagSafe wireless charger for iPhone. Fast and convenient charging.",
                70, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Power", "15W", "Type", "Wireless", "MagSafe", "Yes")),
            
            createProduct("ELEC034", "Ring Light", "Neewer", 29.99, "Electronics", "Accessories",
                "10-inch LED ring light with phone holder. Perfect for content creation.",
                45, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "10 inch", "Power", "USB", "Modes", "3")),
            
            createProduct("ELEC035", "Phone Tripod", "UBeesize", 19.99, "Electronics", "Accessories",
                "Flexible phone tripod with remote. Great for photos and videos.",
                80, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Height", "12 inch", "Remote", "Bluetooth", "Material", "Metal")),
            
            createProduct("ELEC036", "Bluetooth Speaker", "JBL", 49.99, "Electronics", "Accessories",
                "Portable waterproof Bluetooth speaker. Great sound in a compact size.",
                55, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Battery", "12 hours", "Waterproof", "IPX7", "Power", "10W")),
            
            createProduct("ELEC037", "Car Charger", "Anker", 24.99, "Electronics", "Accessories",
                "Dual-port car charger with PowerIQ technology. Fast charging on the go.",
                90, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Ports", "2", "Power", "24W", "Technology", "PowerIQ")),
            
            createProduct("ELEC038", "Phone Stand", "Lamicall", 15.99, "Electronics", "Accessories",
                "Adjustable phone stand for desk. Perfect angle for video calls.",
                110, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Material", "Aluminum", "Adjustable", "270°", "Size", "Universal")),
            
            createProduct("ELEC039", "Power Strip", "Belkin", 29.99, "Electronics", "Accessories",
                "Surge protector power strip with USB ports. Protection for all devices.",
                65, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Outlets", "8", "USB Ports", "4", "Surge Protection", "Yes")),
            
            createProduct("ELEC040", "Wall Mount", "PERLESMITH", 34.99, "Electronics", "Accessories",
                "TV wall mount bracket with full motion. Easy installation and sturdy.",
                40, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Size", "32-55 inch", "Motion", "Full", "Weight Capacity", "88 lbs")),
            
            createProduct("ELEC041", "Cable Organizer", "JOTO", 16.99, "Electronics", "Accessories",
                "Cable management sleeve system. Keep your desk neat and tidy.",
                85, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Length", "20 inch", "Material", "Neoprene", "Pack", "4")),
            
            createProduct("ELEC042", "Smart Plug", "TP-Link", 19.99, "Electronics", "Accessories",
                "WiFi smart plug with energy monitoring. Control devices with your phone.",
                75, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("WiFi", "2.4GHz", "App", "Kasa", "Voice Control", "Yes")),
            
            createProduct("ELEC043", "Keyboard", "Logitech", 49.99, "Electronics", "Accessories",
                "Wireless keyboard with long battery life. Comfortable typing experience.",
                50, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "Wireless", "Battery", "24 months", "Layout", "Full")),
            
            createProduct("ELEC044", "Mouse", "Logitech", 29.99, "Electronics", "Accessories",
                "Wireless mouse with ergonomic design. Precise tracking and comfort.",
                60, 4.5, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("DPI", "1000", "Battery", "12 months", "Buttons", "6")),
            
            createProduct("ELEC045", "Hard Drive", "WD", 79.99, "Electronics", "Accessories",
                "2TB portable hard drive. Fast data transfer and reliable storage.",
                40, 4.7, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Capacity", "2TB", "Interface", "USB 3.0", "Speed", "5Gbps")),
            
            createProduct("ELEC046", "Memory Card", "SanDisk", 24.99, "Electronics", "Accessories",
                "128GB microSD card with adapter. Fast read and write speeds.",
                95, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Capacity", "128GB", "Speed", "A2", "Class", "U3")),
            
            createProduct("ELEC047", "Phone Grip", "PopSockets", 9.99, "Electronics", "Accessories",
                "Phone grip and stand in one. Customizable designs available.",
                120, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Type", "Collapsible", "Material", "Plastic", "Adhesive", "Reusable")),
            
            createProduct("ELEC048", "Webcam", "Logitech", 69.99, "Electronics", "Accessories",
                "1080p webcam with microphone. Perfect for video calls and streaming.",
                35, 4.6, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Resolution", "1080p", "FPS", "30", "Microphone", "Stereo")),
            
            createProduct("ELEC049", "USB Hub", "Sabrent", 19.99, "Electronics", "Accessories",
                "4-port USB 3.0 hub. Expand your connectivity options.",
                70, 4.4, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Ports", "4", "Speed", "5Gbps", "Power", "Individual")),
            
            createProduct("ELEC050", "Cable Ties", "VELCRO", 12.99, "Electronics", "Accessories",
                "Reusable cable ties for organization. Keep cables neat and tidy.",
                100, 4.3, Arrays.asList("https://placehold.co/800x800/6366f1/ffffff", "https://placehold.co/800x800/6366f1/ffffff"),
                Map.of("Length", "8 inch", "Pack", "50", "Reusable", "Yes"))
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
