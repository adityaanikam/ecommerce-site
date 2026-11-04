package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The service-to-service read that order-service makes before it saves an order.
 *
 * It lives under /internal rather than under /products because it is not part of the
 * public API: the gateway does not route /internal/**, so it is reachable only from
 * inside the cluster network. One POST taking all the product ids at once, rather
 * than the caller looping over GET /products/{id}, so placing an order is one
 * round trip whatever the basket size.
 *
 * It returns the catalog's own price and stock. That is the whole point: the browser
 * sends prices too, and those cannot be trusted.
 */
@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class InternalStockController {

    private final ProductService productService;

    @PostMapping("/lookup")
    public ResponseEntity<List<StockView>> lookup(@RequestBody LookupRequest request) {
        List<StockView> views = new ArrayList<>();
        if (request == null || request.productIds() == null) {
            return ResponseEntity.ok(views);
        }
        for (String productId : request.productIds()) {
            Optional<Product> product = productService.findById(productId);
            product.ifPresent(p -> views.add(new StockView(
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    p.isOnSale() && p.getDiscountPrice() > 0 ? p.getDiscountPrice() : p.getPrice(),
                    p.getStock())));
        }
        return ResponseEntity.ok(views);
    }

    public record LookupRequest(List<String> productIds) {
    }

    /**
     * effectivePrice is what the order should actually be charged: the discounted
     * price when the product is on sale, the list price otherwise. Deciding that here
     * rather than in order-service keeps pricing rules in the service that owns them.
     */
    public record StockView(String productId, String name, double price, double effectivePrice, int stock) {
    }
}
