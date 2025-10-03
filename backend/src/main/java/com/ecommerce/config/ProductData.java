package com.ecommerce.config;

import com.ecommerce.model.Product;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProductData {
    
    public static List<Product> getAllProducts() {
        return Arrays.asList(
            // ELECTRONICS - Mobiles (11 products)
            createProduct("ELEC001", "iPhone 15 Pro", "Apple", 999.0, "Electronics", "Mobiles", 
                "Latest iPhone with titanium design and A17 Pro chip, offering unparalleled performance and camera capabilities.", 
                50, 4.8, Arrays.asList("https://picsum.photos/seed/ELEC001/300/300", "https://picsum.photos/seed/ELEC001a/300/300"),
                Map.of("Display", "6.1-inch Super Retina XDR", "Processor", "A17 Pro Bionic", "Storage", "128GB")),
            
            createProduct("ELEC002", "Samsung Galaxy S24", "Samsung", 899.0, "Electronics", "Mobiles",
                "Premium Android phone with advanced AI features and a stunning AMOLED display.",
                45, 4.7, Arrays.asList("https://picsum.photos/seed/ELEC002/300/300", "https://picsum.photos/seed/ELEC002a/300/300"),
                Map.of("Display", "6.2-inch Dynamic AMOLED 2X", "Processor", "Snapdragon 8 Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC003", "Google Pixel 8", "Google", 699.0, "Electronics", "Mobiles",
                "Pure Android experience with an amazing computational photography camera and Tensor G3 chip.",
                40, 4.6, Arrays.asList("https://picsum.photos/seed/ELEC003/300/300", "https://picsum.photos/seed/ELEC003a/300/300"),
                Map.of("Display", "6.2-inch OLED", "Processor", "Google Tensor G3", "Storage", "128GB")),
            
            createProduct("ELEC004", "Honor Magic 6", "Honor", 649.0, "Electronics", "Mobiles",
                "Flagship smartphone from Honor, featuring a powerful camera system and sleek design.",
                35, 4.5, Arrays.asList("https://picsum.photos/seed/ELEC004/300/300", "https://picsum.photos/seed/ELEC004a/300/300"),
                Map.of("Display", "6.78-inch OLED", "Processor", "Snapdragon 8 Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC005", "Xiaomi 14", "Xiaomi", 599.0, "Electronics", "Mobiles",
                "Compact flagship with Leica optics, offering a premium photography experience.",
                30, 4.6, Arrays.asList("https://picsum.photos/seed/ELEC005/300/300", "https://picsum.photos/seed/ELEC005a/300/300"),
                Map.of("Display", "6.36-inch AMOLED", "Processor", "Snapdragon 8 Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC006", "Realme GT 6", "Realme", 449.0, "Electronics", "Mobiles",
                "High-performance smartphone with a focus on gaming and fast charging.",
                55, 4.4, Arrays.asList("https://picsum.photos/seed/ELEC006/300/300", "https://picsum.photos/seed/ELEC006a/300/300"),
                Map.of("Display", "6.78-inch AMOLED", "Processor", "Snapdragon 8s Gen 3", "Storage", "256GB")),
            
            createProduct("ELEC007", "iPhone 14", "Apple", 649.0, "Electronics", "Mobiles",
                "Reliable and powerful iPhone with a great camera and long-lasting battery.",
                60, 4.7, Arrays.asList("https://picsum.photos/seed/ELEC007/300/300", "https://picsum.photos/seed/ELEC007a/300/300"),
                Map.of("Display", "6.1-inch Super Retina XDR", "Processor", "A15 Bionic", "Storage", "128GB")),
            
            createProduct("ELEC008", "Samsung A55", "Samsung", 399.0, "Electronics", "Mobiles",
                "Mid-range Samsung phone with a vibrant display and solid performance for everyday use.",
                70, 4.3, Arrays.asList("https://picsum.photos/seed/ELEC008/300/300", "https://picsum.photos/seed/ELEC008a/300/300"),
                Map.of("Display", "6.6-inch Super AMOLED", "Processor", "Exynos 1480", "Storage", "128GB")),
            
            createProduct("ELEC009", "Redmi Note 13", "Xiaomi", 299.0, "Electronics", "Mobiles",
                "Budget-friendly smartphone with a large display and decent camera for its price segment.",
                80, 4.2, Arrays.asList("https://picsum.photos/seed/ELEC009/300/300", "https://picsum.photos/seed/ELEC009a/300/300"),
                Map.of("Display", "6.67-inch AMOLED", "Processor", "Snapdragon 685", "Storage", "128GB")),
            
            createProduct("ELEC010", "Poco X6", "Xiaomi", 249.0, "Electronics", "Mobiles",
                "Gaming-focused phone with a powerful processor and high refresh rate display.",
                65, 4.4, Arrays.asList("https://picsum.photos/seed/ELEC010/300/300", "https://picsum.photos/seed/ELEC010a/300/300"),
                Map.of("Display", "6.67-inch AMOLED", "Processor", "Snapdragon 7s Gen 2", "Storage", "256GB")),
            
            createProduct("ELEC011", "Oppo Reno 11", "Oppo", 359.0, "Electronics", "Mobiles",
                "Stylish smartphone with a focus on camera and fast charging capabilities.",
                50, 4.3, Arrays.asList("https://picsum.photos/seed/ELEC011/300/300", "https://picsum.photos/seed/ELEC011a/300/300"),
                Map.of("Display", "6.7-inch AMOLED", "Processor", "Dimensity 8200", "Storage", "256GB")),
            
            // ELECTRONICS - Headphones (10 products)
            createProduct("ELEC012", "Sony WH-1000XM5", "Sony", 399.0, "Electronics", "Headphones",
                "Industry-leading noise cancellation headphones with exceptional sound quality and comfort.",
                60, 4.8, Arrays.asList("https://picsum.photos/seed/ELEC012/300/300", "https://picsum.photos/seed/ELEC012a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Bluetooth 5.2", "Battery Life", "30 hours")),
            
            createProduct("ELEC013", "AirPods Pro 2nd Gen", "Apple", 249.0, "Electronics", "Headphones",
                "Wireless earbuds with active noise cancellation, transparency mode, and spatial audio.",
                75, 4.7, Arrays.asList("https://picsum.photos/seed/ELEC013/300/300", "https://picsum.photos/seed/ELEC013a/300/300"),
                Map.of("Type", "In-ear", "Connectivity", "Bluetooth 5.3", "Battery Life", "6 hours (earbuds)")),
            
            createProduct("ELEC014", "Bose QuietComfort 45", "Bose", 329.0, "Electronics", "Headphones",
                "Renowned for their comfortable fit and powerful noise-cancelling performance.",
                55, 4.6, Arrays.asList("https://picsum.photos/seed/ELEC014/300/300", "https://picsum.photos/seed/ELEC014a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Bluetooth 5.1", "Battery Life", "24 hours")),
            
            createProduct("ELEC015", "JBL Live 650BTNC", "JBL", 149.0, "Electronics", "Headphones",
                "Affordable over-ear headphones with active noise cancellation and JBL Signature Sound.",
                80, 4.3, Arrays.asList("https://picsum.photos/seed/ELEC015/300/300", "https://picsum.photos/seed/ELEC015a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Bluetooth 4.2", "Battery Life", "30 hours")),
            
            createProduct("ELEC016", "Sennheiser HD 450BT", "Sennheiser", 199.0, "Electronics", "Headphones",
                "Wireless headphones delivering great sound with active noise cancellation and intuitive controls.",
                65, 4.4, Arrays.asList("https://picsum.photos/seed/ELEC016/300/300", "https://picsum.photos/seed/ELEC016a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Bluetooth 5.0", "Battery Life", "30 hours")),
            
            createProduct("ELEC017", "Audio Technica ATH-M50x", "Audio Technica", 149.0, "Electronics", "Headphones",
                "Critically acclaimed professional monitor headphones, known for their accurate audio and comfort.",
                70, 4.7, Arrays.asList("https://picsum.photos/seed/ELEC017/300/300", "https://picsum.photos/seed/ELEC017a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Wired", "Frequency Response", "15-28,000 Hz")),
            
            createProduct("ELEC018", "Beats Studio 3", "Beats", 199.0, "Electronics", "Headphones",
                "Wireless over-ear headphones with Pure ANC and Apple W1 chip for seamless connectivity.",
                50, 4.5, Arrays.asList("https://picsum.photos/seed/ELEC018/300/300", "https://picsum.photos/seed/ELEC018a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Bluetooth", "Battery Life", "22 hours")),
            
            createProduct("ELEC019", "Skullcandy Crusher", "Skullcandy", 129.0, "Electronics", "Headphones",
                "Headphones with sensory bass that delivers an immersive audio experience.",
                40, 4.2, Arrays.asList("https://picsum.photos/seed/ELEC019/300/300", "https://picsum.photos/seed/ELEC019a/300/300"),
                Map.of("Type", "Over-ear", "Connectivity", "Bluetooth", "Battery Life", "40 hours")),
            
            createProduct("ELEC020", "Marshall Major IV", "Marshall", 149.0, "Electronics", "Headphones",
                "Iconic on-ear headphones with over 80 hours of wireless playtime and wireless charging.",
                45, 4.6, Arrays.asList("https://picsum.photos/seed/ELEC020/300/300", "https://picsum.photos/seed/ELEC020a/300/300"),
                Map.of("Type", "On-ear", "Connectivity", "Bluetooth 5.0", "Battery Life", "80+ hours")),
            
            createProduct("ELEC021", "Plantronics BackBeat", "Plantronics", 99.0, "Electronics", "Headphones",
                "Comfortable and durable wireless headphones, perfect for active lifestyles.",
                30, 4.1, Arrays.asList("https://picsum.photos/seed/ELEC021/300/300", "https://picsum.photos/seed/ELEC021a/300/300"),
                Map.of("Type", "On-ear", "Connectivity", "Bluetooth", "Battery Life", "18 hours")),
            
            // FASHION - T-shirts (10 products)
            createProduct("FASH001", "Cotton Crew Neck Tee", "BasicWear", 19.0, "Fashion", "T-shirts",
                "Comfortable cotton t-shirt for everyday wear with a classic crew neck design.",
                100, 4.2, Arrays.asList("https://picsum.photos/seed/FASH001/300/300", "https://picsum.photos/seed/FASH001a/300/300"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH002", "Graphic Print Tee", "UrbanStyle", 25.0, "Fashion", "T-shirts",
                "Trendy graphic t-shirt with unique designs perfect for casual outings.",
                80, 4.3, Arrays.asList("https://picsum.photos/seed/FASH002/300/300", "https://picsum.photos/seed/FASH002a/300/300"),
                Map.of("Material", "Cotton Blend", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH003", "Polo Shirt", "ClassicWear", 35.0, "Fashion", "T-shirts",
                "Classic polo shirt with a sophisticated look, perfect for smart casual occasions.",
                60, 4.4, Arrays.asList("https://picsum.photos/seed/FASH003/300/300", "https://picsum.photos/seed/FASH003a/300/300"),
                Map.of("Material", "Pique Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH004", "Oversized Fit Tee", "ComfortZone", 22.0, "Fashion", "T-shirts",
                "Relaxed fit t-shirt with a modern oversized silhouette for ultimate comfort.",
                90, 4.1, Arrays.asList("https://picsum.photos/seed/FASH004/300/300", "https://picsum.photos/seed/FASH004a/300/300"),
                Map.of("Material", "100% Cotton", "Fit", "Oversized", "Care", "Machine Wash")),
            
            createProduct("FASH005", "V-Neck Tee", "ElegantWear", 24.0, "Fashion", "T-shirts",
                "Classic v-neck t-shirt with a flattering neckline and comfortable fit.",
                75, 4.3, Arrays.asList("https://picsum.photos/seed/FASH005/300/300", "https://picsum.photos/seed/FASH005a/300/300"),
                Map.of("Material", "Cotton Blend", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH006", "Long Sleeve Tee", "WarmWear", 28.0, "Fashion", "T-shirts",
                "Versatile long sleeve t-shirt perfect for layering or wearing alone.",
                65, 4.2, Arrays.asList("https://picsum.photos/seed/FASH006/300/300", "https://picsum.photos/seed/FASH006a/300/300"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH007", "Henley Shirt", "CasualStyle", 32.0, "Fashion", "T-shirts",
                "Classic henley shirt with button placket and comfortable cotton construction.",
                55, 4.4, Arrays.asList("https://picsum.photos/seed/FASH007/300/300", "https://picsum.photos/seed/FASH007a/300/300"),
                Map.of("Material", "Cotton Blend", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH008", "Basic White Tee", "EssentialWear", 18.0, "Fashion", "T-shirts",
                "Essential white t-shirt that's perfect for any wardrobe and occasion.",
                120, 4.0, Arrays.asList("https://picsum.photos/seed/FASH008/300/300", "https://picsum.photos/seed/FASH008a/300/300"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH009", "Striped Tee", "NauticalStyle", 26.0, "Fashion", "T-shirts",
                "Classic striped t-shirt with a timeless nautical-inspired design.",
                70, 4.3, Arrays.asList("https://picsum.photos/seed/FASH009/300/300", "https://picsum.photos/seed/FASH009a/300/300"),
                Map.of("Material", "Cotton Blend", "Fit", "Regular", "Care", "Machine Wash")),
            
            createProduct("FASH010", "Vintage Wash Tee", "RetroWear", 30.0, "Fashion", "T-shirts",
                "Vintage-inspired t-shirt with a soft, worn-in feel and retro styling.",
                50, 4.5, Arrays.asList("https://picsum.photos/seed/FASH010/300/300", "https://picsum.photos/seed/FASH010a/300/300"),
                Map.of("Material", "100% Cotton", "Fit", "Regular", "Care", "Machine Wash")),
            
            // HOME & GARDEN - Curtains (10 products)
            createProduct("HOME001", "Blackout Curtains", "HomeStyle", 45.0, "Home & Garden", "Curtains",
                "Room darkening curtains for better sleep and privacy with elegant design.",
                40, 4.5, Arrays.asList("https://picsum.photos/seed/HOME001/300/300", "https://picsum.photos/seed/HOME001a/300/300"),
                Map.of("Material", "Polyester", "Light Blocking", "100%", "Care", "Machine Wash")),
            
            createProduct("HOME002", "Sheer Curtains", "LightStyle", 25.0, "Home & Garden", "Curtains",
                "Light and airy sheer curtains that let in natural light while maintaining privacy.",
                50, 4.3, Arrays.asList("https://picsum.photos/seed/HOME002/300/300", "https://picsum.photos/seed/HOME002a/300/300"),
                Map.of("Material", "Sheer Fabric", "Light Blocking", "30%", "Care", "Hand Wash")),
            
            createProduct("HOME003", "Thermal Curtains", "EnergySaver", 55.0, "Home & Garden", "Curtains",
                "Energy-efficient thermal curtains that help regulate room temperature.",
                35, 4.6, Arrays.asList("https://picsum.photos/seed/HOME003/300/300", "https://picsum.photos/seed/HOME003a/300/300"),
                Map.of("Material", "Thermal Fabric", "Insulation", "High", "Care", "Machine Wash")),
            
            createProduct("HOME004", "Room Divider", "SpaceMax", 89.0, "Home & Garden", "Curtains",
                "Versatile room divider curtain for creating separate spaces in open areas.",
                25, 4.4, Arrays.asList("https://picsum.photos/seed/HOME004/300/300", "https://picsum.photos/seed/HOME004a/300/300"),
                Map.of("Material", "Heavy Fabric", "Height", "8 feet", "Care", "Spot Clean")),
            
            createProduct("HOME005", "Window Valance", "ElegantHome", 35.0, "Home & Garden", "Curtains",
                "Decorative window valance to add style and elegance to any window.",
                60, 4.2, Arrays.asList("https://picsum.photos/seed/HOME005/300/300", "https://picsum.photos/seed/HOME005a/300/300"),
                Map.of("Material", "Decorative Fabric", "Style", "Traditional", "Care", "Dry Clean")),
            
            // SPORTS - Gym Equipment (10 products)
            createProduct("SPORT001", "Adjustable Dumbbells", "FitPro", 199.0, "Sports", "Gym Equipment",
                "Versatile adjustable dumbbells that replace multiple sets, perfect for home gyms.",
                25, 4.6, Arrays.asList("https://picsum.photos/seed/SPORT001/300/300", "https://picsum.photos/seed/SPORT001a/300/300"),
                Map.of("Weight Range", "5-50 lbs", "Material", "Cast Iron", "Grip", "Rubber Coated")),
            
            createProduct("SPORT002", "Resistance Bands", "FlexFit", 29.0, "Sports", "Gym Equipment",
                "Set of resistance bands for full-body workouts, portable and versatile.",
                100, 4.4, Arrays.asList("https://picsum.photos/seed/SPORT002/300/300", "https://picsum.photos/seed/SPORT002a/300/300"),
                Map.of("Resistance", "5-50 lbs", "Material", "Latex", "Set", "5 Bands")),
            
            createProduct("SPORT003", "Kettlebell 20lb", "IronFit", 45.0, "Sports", "Gym Equipment",
                "High-quality cast iron kettlebell for strength training and cardio workouts.",
                40, 4.5, Arrays.asList("https://picsum.photos/seed/SPORT003/300/300", "https://picsum.photos/seed/SPORT003a/300/300"),
                Map.of("Weight", "20 lbs", "Material", "Cast Iron", "Grip", "Textured Handle")),
            
            createProduct("SPORT004", "Pull-up Bar", "HomeGym", 35.0, "Sports", "Gym Equipment",
                "Doorway pull-up bar for upper body strength training at home.",
                30, 4.3, Arrays.asList("https://picsum.photos/seed/SPORT004/300/300", "https://picsum.photos/seed/SPORT004a/300/300"),
                Map.of("Installation", "Doorway", "Weight Capacity", "300 lbs", "Material", "Steel")),
            
            createProduct("SPORT005", "Ab Wheel", "CoreMax", 25.0, "Sports", "Gym Equipment",
                "Ab wheel for core strengthening exercises and full-body workouts.",
                80, 4.2, Arrays.asList("https://picsum.photos/seed/SPORT005/300/300", "https://picsum.photos/seed/SPORT005a/300/300"),
                Map.of("Material", "Plastic & Steel", "Grip", "Ergonomic", "Wheels", "Dual"))
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
