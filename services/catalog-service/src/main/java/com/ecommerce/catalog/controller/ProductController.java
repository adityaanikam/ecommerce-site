package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Routes are unchanged from the monolith, which served them at the root rather than
 * under /api (see docs/AUDIT.md). The gateway owns the /api prefix and strips it, so
 * /api/products/x arrives here as /products/x.
 *
 * There is no @CrossOrigin here on purpose. CORS is handled once, at the gateway,
 * from an environment variable. The monolith declared allowed origins in five places,
 * four of them hardcoded.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(
                productService.findProducts(category, subcategory, search, minPrice, maxPrice, pageRequest));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.findFeaturedProducts());
    }

    @GetMapping("/new")
    public ResponseEntity<List<Product>> getNewProducts() {
        return ResponseEntity.ok(productService.findNewProducts());
    }

    @GetMapping("/on-sale")
    public ResponseEntity<List<Product>> getOnSaleProducts() {
        return ResponseEntity.ok(productService.findOnSaleProducts());
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(productService.findByCategory(category, PageRequest.of(page, size)));
    }

    /**
     * Declared after the fixed sub-paths above so that /products/featured is not
     * swallowed by {id}. Spring orders the more specific pattern first regardless,
     * but keeping the declaration order honest avoids surprises.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
