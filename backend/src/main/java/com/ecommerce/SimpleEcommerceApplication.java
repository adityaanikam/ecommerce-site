package com.ecommerce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ecommerce.repository.ProductRepository;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class SimpleEcommerceApplication {

	@Autowired
	private ProductRepository productRepository;

	public static void main(String[] args) {
		SpringApplication.run(SimpleEcommerceApplication.class, args);
	}


	@GetMapping("/api/categories")
	public java.util.List<Map<String, Object>> categories() {
		// Get actual product counts from database
		long electronicsCount = productRepository.countByCategory("Electronics");
		long fashionCount = productRepository.countByCategory("Fashion");
		long sportsCount = productRepository.countByCategory("Sports");
		long homeCount = productRepository.countByCategory("Home & Garden");
		
		java.util.List<Map<String, Object>> categories = new java.util.ArrayList<>();
		
		// Electronics category
		Map<String, Object> electronics = new java.util.HashMap<>();
		electronics.put("name", "Electronics");
		electronics.put("slug", "electronics");
		java.util.List<Map<String, Object>> electronicsSubs = new java.util.ArrayList<>();
		electronicsSubs.add(Map.of("name", "Smartphones", "slug", "smartphones", "count", electronicsCount/2));
		electronicsSubs.add(Map.of("name", "Laptops", "slug", "laptops", "count", electronicsCount/2));
		electronics.put("subcategories", electronicsSubs);
		categories.add(electronics);
		
		// Fashion category
		Map<String, Object> fashion = new java.util.HashMap<>();
		fashion.put("name", "Fashion");
		fashion.put("slug", "fashion");
		java.util.List<Map<String, Object>> fashionSubs = new java.util.ArrayList<>();
		fashionSubs.add(Map.of("name", "T-shirts", "slug", "t-shirts", "count", fashionCount/2));
		fashionSubs.add(Map.of("name", "Jeans", "slug", "jeans", "count", fashionCount/2));
		fashion.put("subcategories", fashionSubs);
		categories.add(fashion);
		
		// Sports category
		Map<String, Object> sports = new java.util.HashMap<>();
		sports.put("name", "Sports");
		sports.put("slug", "sports");
		java.util.List<Map<String, Object>> sportsSubs = new java.util.ArrayList<>();
		sportsSubs.add(Map.of("name", "Gym Equipment", "slug", "gym-equipment", "count", sportsCount/2));
		sportsSubs.add(Map.of("name", "Running", "slug", "running", "count", sportsCount/2));
		sports.put("subcategories", sportsSubs);
		categories.add(sports);
		
		// Home & Garden category
		Map<String, Object> home = new java.util.HashMap<>();
		home.put("name", "Home & Garden");
		home.put("slug", "home-garden");
		java.util.List<Map<String, Object>> homeSubs = new java.util.ArrayList<>();
		homeSubs.add(Map.of("name", "Curtains", "slug", "curtains", "count", homeCount/2));
		homeSubs.add(Map.of("name", "Furniture", "slug", "furniture", "count", homeCount/2));
		home.put("subcategories", homeSubs);
		categories.add(home);
		
		return categories;
	}
}