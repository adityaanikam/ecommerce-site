package com.ecommerce.controller;

import com.ecommerce.dto.CartDto;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CartController {
    
    private final CartService cartService;
    
    // Cart Operations
    
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> getCart(Authentication authentication) {
        log.info("Get cart request received");
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> addItemToCart(@Valid @RequestBody CartDto.AddItemRequest request,
                                                            Authentication authentication) {
        log.info("Add item to cart request received for product ID: {}", request.getProductId());
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }
    
    @PutMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> updateItemQuantity(@PathVariable String productId,
                                                                 @RequestParam Integer quantity,
                                                                 Authentication authentication) {
        log.info("Update item quantity request received for product ID: {} to quantity: {}", productId, quantity);
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.updateItemQuantity(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> removeItemFromCart(@PathVariable String productId,
                                                                 Authentication authentication) {
        log.info("Remove item from cart request received for product ID: {}", productId);
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> clearCart(Authentication authentication) {
        log.info("Clear cart request received");
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }
    
    // Cart Validation and Processing
    
    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> validateCart(Authentication authentication) {
        log.info("Validate cart request received");
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.validateCart(userId);
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.MessageResponse> saveCart(@Valid @RequestBody CartDto.CartResponse cartResponse,
                                                          Authentication authentication) {
        log.info("Save cart request received");
        String userId = authentication.getName();
        cartService.saveCart(userId, cartResponse);
        return ResponseEntity.ok(CartDto.MessageResponse.builder()
                .message("Cart saved successfully")
                .build());
    }
    
    // Coupon Management
    
    @PostMapping("/coupon")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> applyCoupon(@RequestParam String couponCode,
                                                          Authentication authentication) {
        log.info("Apply coupon request received with code: {}", couponCode);
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.applyCoupon(userId, couponCode);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/coupon")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartResponse> removeCoupon(Authentication authentication) {
        log.info("Remove coupon request received");
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.removeCoupon(userId);
        return ResponseEntity.ok(cart);
    }
    
    // Cart Summary and Statistics
    
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartSummaryResponse> getCartSummary(Authentication authentication) {
        log.info("Get cart summary request received");
        String userId = authentication.getName();
        CartDto.CartResponse cart = cartService.getCartByUserId(userId);
        
        CartDto.CartSummaryResponse summary = CartDto.CartSummaryResponse.builder()
                .itemCount(cart.getItems().size())
                .totalItems(cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum())
                .subtotal(cart.getItems().stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum())
                .taxAmount(cart.getTaxAmount())
                .shippingAmount(cart.getShippingAmount())
                .discountAmount(cart.getDiscountAmount())
                .totalAmount(cart.getTotalAmount())
                .build();
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/item-count")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<CartDto.CartItemCountResponse> getCartItemCount(Authentication authentication) {
        log.info("Get cart item count request received");
        String userId = authentication.getName();
        int itemCount = cartService.getCartItemCount(userId);
        
        CartDto.CartItemCountResponse response = CartDto.CartItemCountResponse.builder()
                .itemCount(itemCount)
                .isEmpty(cartService.isCartEmpty(userId))
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Admin Operations
    
    @GetMapping("/abandoned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CartDto.CartResponse>> getAbandonedCarts(@RequestParam(defaultValue = "7") int daysSinceLastUpdate) {
        log.info("Get abandoned carts request received for days: {}", daysSinceLastUpdate);
        List<CartDto.CartResponse> abandonedCarts = cartService.getAbandonedCarts(daysSinceLastUpdate);
        return ResponseEntity.ok(abandonedCarts);
    }
    
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CartDto.MessageResponse> cleanupExpiredCarts(@RequestParam(defaultValue = "30") int daysSinceLastUpdate) {
        log.info("Cleanup expired carts request received for days: {}", daysSinceLastUpdate);
        cartService.cleanupExpiredCarts(daysSinceLastUpdate);
        return ResponseEntity.ok(CartDto.MessageResponse.builder()
                .message("Expired carts cleaned up successfully")
                .build());
    }
}