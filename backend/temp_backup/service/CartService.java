package com.ecommerce.service;

import com.ecommerce.dto.CartDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CacheService cacheService;
    
    // Cart Operations
    
    public CartDto.CartResponse getCartByUserId(String userId) {
        log.debug("Fetching cart for user ID: {}", userId);
        
        // Try cache first
        CartDto.CartResponse cached = cacheService.getCartFromCache(userId);
        if (cached != null) {
            return cached;
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        
        CartDto.CartResponse response = mapToCartResponse(cart);
        
        // Cache the result
        cacheService.cacheCart(userId, response);
        
        return response;
    }
    
    public CartDto.CartResponse addItemToCart(String userId, CartDto.AddItemRequest request) {
        log.info("Adding item to cart for user ID: {}", userId);
        
        // Validate product exists and is available
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        if (!product.getIsActive()) {
            throw new ValidationException("Product is not available");
        }
        
        if (product.getStock() < request.getQuantity()) {
            throw new ValidationException("Insufficient stock. Available: " + product.getStock() + ", Requested: " + request.getQuantity());
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        
        // Check if item already exists in cart
        Optional<Cart.CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            Cart.CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            if (product.getStock() < newQuantity) {
                throw new ValidationException("Insufficient stock. Available: " + product.getStock() + ", Requested: " + newQuantity);
            }
            
            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            // Add new item
            Cart.CartItem newItem = Cart.CartItem.builder()
                    .productId(request.getProductId())
                    .productName(product.getName())
                    .productImage(product.getImages().isEmpty() ? null : product.getImages().get(0))
                    .price(product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice())
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            cart.getItems().add(newItem);
        }
        
        // Recalculate totals
        recalculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        
        log.info("Item added to cart successfully for user ID: {}", userId);
        return mapToCartResponse(savedCart);
    }
    
    public CartDto.CartResponse updateItemQuantity(String userId, String productId, Integer quantity) {
        log.info("Updating item quantity in cart for user ID: {}", userId);
        
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        
        Cart.CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));
        
        // Validate stock availability
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (product.getStock() < quantity) {
            throw new ValidationException("Insufficient stock. Available: " + product.getStock() + ", Requested: " + quantity);
        }
        
        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());
        
        // Recalculate totals
        recalculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        
        log.info("Item quantity updated successfully for user ID: {}", userId);
        return mapToCartResponse(savedCart);
    }
    
    public CartDto.CartResponse removeItemFromCart(String userId, String productId) {
        log.info("Removing item from cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Item not found in cart");
        }
        
        // Recalculate totals
        recalculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        
        log.info("Item removed from cart successfully for user ID: {}", userId);
        return mapToCartResponse(savedCart);
    }
    
    public CartDto.CartResponse clearCart(String userId) {
        log.info("Clearing cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cart.setTaxAmount(0.0);
        cart.setShippingAmount(0.0);
        cart.setDiscountAmount(0.0);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        
        log.info("Cart cleared successfully for user ID: {}", userId);
        return mapToCartResponse(savedCart);
    }
    
    // Cart Persistence and Abandonment Handling
    
    public void saveCart(String userId, CartDto.CartResponse cartResponse) {
        log.debug("Saving cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        
        // Update cart with new data
        cart.setTotalAmount(cartResponse.getTotalAmount());
        cart.setTaxAmount(cartResponse.getTaxAmount());
        cart.setShippingAmount(cartResponse.getShippingAmount());
        cart.setDiscountAmount(cartResponse.getDiscountAmount());
        cart.setUpdatedAt(LocalDateTime.now());
        
        cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
    }
    
    public List<CartDto.CartResponse> getAbandonedCarts(int daysSinceLastUpdate) {
        log.debug("Fetching abandoned carts older than {} days", daysSinceLastUpdate);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastUpdate);
        List<Cart> abandonedCarts = cartRepository.findByUpdatedAtBeforeAndItemsIsNotEmpty(cutoffDate);
        
        return abandonedCarts.stream()
                .map(this::mapToCartResponse)
                .collect(Collectors.toList());
    }
    
    public void cleanupExpiredCarts(int daysSinceLastUpdate) {
        log.info("Cleaning up expired carts older than {} days", daysSinceLastUpdate);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastUpdate);
        List<Cart> expiredCarts = cartRepository.findByUpdatedAtBeforeAndItemsIsNotEmpty(cutoffDate);
        
        for (Cart cart : expiredCarts) {
            cart.getItems().clear();
            cart.setTotalAmount(0.0);
            cart.setTaxAmount(0.0);
            cart.setShippingAmount(0.0);
            cart.setDiscountAmount(0.0);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            
            // Clear cache
            cacheService.evictCartCache(cart.getUserId());
        }
        
        log.info("Cleaned up {} expired carts", expiredCarts.size());
    }
    
    // Cart Validation and Processing
    
    public CartDto.CartResponse validateCart(String userId) {
        log.debug("Validating cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        
        boolean cartModified = false;
        
        // Validate each item
        cart.getItems().removeIf(item -> {
            try {
                Product product = productRepository.findById(item.getProductId())
                        .orElse(null);
                
                if (product == null || !product.getIsActive()) {
                    log.warn("Removing inactive product {} from cart", item.getProductId());
                    return true;
                }
                
                if (product.getStock() < item.getQuantity()) {
                    log.warn("Adjusting quantity for product {} from {} to {}", 
                            item.getProductId(), item.getQuantity(), product.getStock());
                    item.setQuantity(product.getStock());
                    item.setUpdatedAt(LocalDateTime.now());
                }
                
                // Update price if changed
                Double currentPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
                if (!item.getPrice().equals(currentPrice)) {
                    log.warn("Updating price for product {} from {} to {}", 
                            item.getProductId(), item.getPrice(), currentPrice);
                    item.setPrice(currentPrice);
                    item.setUpdatedAt(LocalDateTime.now());
                }
                
                return false;
            } catch (Exception e) {
                log.error("Error validating cart item: {}", item.getProductId(), e);
                return true;
            }
        });
        
        if (cartModified) {
            // Recalculate totals
            recalculateCartTotals(cart);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            
            // Clear cache
            cacheService.evictCartCache(userId);
        }
        
        return mapToCartResponse(cart);
    }
    
    public CartDto.CartResponse applyCoupon(String userId, String couponCode) {
        log.info("Applying coupon {} to cart for user ID: {}", couponCode, userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        
        // TODO: Implement coupon validation logic
        // For now, we'll just set a placeholder discount
        double discountAmount = cart.getTotalAmount() * 0.1; // 10% discount
        
        cart.setDiscountAmount(discountAmount);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        
        log.info("Coupon applied successfully for user ID: {}", userId);
        return mapToCartResponse(savedCart);
    }
    
    public CartDto.CartResponse removeCoupon(String userId) {
        log.info("Removing coupon from cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        
        cart.setDiscountAmount(0.0);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart savedCart = cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        
        log.info("Coupon removed successfully for user ID: {}", userId);
        return mapToCartResponse(savedCart);
    }
    
    // Utility Methods
    
    private Cart createEmptyCart(String userId) {
        log.debug("Creating empty cart for user ID: {}", userId);
        
        Cart cart = Cart.builder()
                .userId(userId)
                .totalAmount(0.0)
                .taxAmount(0.0)
                .shippingAmount(0.0)
                .discountAmount(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return cartRepository.save(cart);
    }
    
    private void recalculateCartTotals(Cart cart) {
        double subtotal = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        // Calculate tax (assuming 10% tax rate)
        double taxAmount = subtotal * 0.1;
        
        // Calculate shipping (free shipping over $50)
        double shippingAmount = subtotal >= 50.0 ? 0.0 : 10.0;
        
        // Calculate total
        double totalAmount = subtotal + taxAmount + shippingAmount - cart.getDiscountAmount();
        
        cart.setTotalAmount(Math.max(0, totalAmount));
        cart.setTaxAmount(taxAmount);
        cart.setShippingAmount(shippingAmount);
    }
    
    private CartDto.CartResponse mapToCartResponse(Cart cart) {
        return CartDto.CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems())
                .totalAmount(cart.getTotalAmount())
                .taxAmount(cart.getTaxAmount())
                .shippingAmount(cart.getShippingAmount())
                .discountAmount(cart.getDiscountAmount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
    
    public int getCartItemCount(String userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return 0;
        }
        
        return cart.getItems().stream()
                .mapToInt(Cart.CartItem::getQuantity)
                .sum();
    }
    
    public boolean isCartEmpty(String userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        return cart == null || cart.getItems().isEmpty();
    }
}