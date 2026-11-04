package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Category endpoints, carried over with the same response shapes the frontend already
 * parses. The category list itself stays a fixed list of the four merchandised
 * categories, as in the monolith, but the counts are read from the database instead
 * of being hardcoded.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    /** The four merchandised categories, in display order, with their subcategories. */
    private static final List<CategoryDefinition> CATEGORIES = List.of(
            new CategoryDefinition("Electronics", "electronics",
                    List.of(new SubcategoryDefinition("Smartphones", "smartphones"),
                            new SubcategoryDefinition("Laptops", "laptops"))),
            new CategoryDefinition("Fashion", "fashion",
                    List.of(new SubcategoryDefinition("T-shirts", "t-shirts"),
                            new SubcategoryDefinition("Jeans", "jeans"))),
            new CategoryDefinition("Sports", "sports",
                    List.of(new SubcategoryDefinition("Gym Equipment", "gym-equipment"),
                            new SubcategoryDefinition("Running", "running"))),
            new CategoryDefinition("Home & Garden", "home-garden",
                    List.of(new SubcategoryDefinition("Curtains", "curtains"),
                            new SubcategoryDefinition("Furniture", "furniture")))
    );

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> body = new ArrayList<>();
        for (CategoryDefinition definition : CATEGORIES) {
            long count = productService.countByCategory(definition.name());
            Map<String, Object> category = new HashMap<>();
            category.put("name", definition.name());
            category.put("slug", definition.slug());
            List<Map<String, Object>> subcategories = new ArrayList<>();
            for (SubcategoryDefinition sub : definition.subcategories()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("name", sub.name());
                entry.put("slug", sub.slug());
                // The monolith split the category count evenly across the two
                // subcategories. Kept as is so the badges in the UI do not change.
                entry.put("count", count / definition.subcategories().size());
                subcategories.add(entry);
            }
            category.put("subcategories", subcategories);
            body.add(category);
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        long total = 0;
        for (CategoryDefinition definition : CATEGORIES) {
            total += productService.countByCategory(definition.name());
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCategories", CATEGORIES.size());
        stats.put("totalProducts", total);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getCategoryBySlug(@PathVariable String slug) {
        return CATEGORIES.stream()
                .filter(c -> c.slug().equalsIgnoreCase(slug))
                .findFirst()
                .map(definition -> {
                    Map<String, Object> category = new HashMap<>();
                    category.put("name", definition.name());
                    category.put("slug", definition.slug());
                    category.put("productCount", productService.countByCategory(definition.name()));
                    List<Map<String, Object>> subcategories = new ArrayList<>();
                    for (SubcategoryDefinition sub : definition.subcategories()) {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("name", sub.name());
                        entry.put("slug", sub.slug());
                        subcategories.add(entry);
                    }
                    category.put("subcategories", subcategories);
                    return ResponseEntity.ok(category);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private record CategoryDefinition(String name, String slug, List<SubcategoryDefinition> subcategories) {
    }

    private record SubcategoryDefinition(String name, String slug) {
    }
}
