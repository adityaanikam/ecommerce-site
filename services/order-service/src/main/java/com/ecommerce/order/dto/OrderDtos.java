package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * The request shape is the cart the frontend already builds plus the address and
 * payment fields its CreateOrderRequest already declares.
 *
 * The frontend's CartContext keeps items as
 * {productId, quantity, name, price, image, stock} in localStorage, and its
 * CreateOrderRequest carries shippingAddress, paymentMethod, shippingMethod and
 * notes but no items, because that type was written assuming a server-side cart
 * that never got built. So the request here is the union: the items from the cart
 * and the rest from CreateOrderRequest.
 *
 * Only productId and quantity are read from each line. The name, price and stock
 * the browser sends are ignored, because they come from the client and the client
 * can say anything. The real figures are fetched from catalog-service.
 */
public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotEmpty(message = "An order must contain at least one item")
            @Valid
            List<Line> items,

            @NotNull(message = "A shipping address is required")
            @Valid
            Address shippingAddress,

            @Valid
            Address billingAddress,

            @NotNull(message = "A payment method is required")
            @Valid
            PaymentMethod paymentMethod,

            @Size(max = 64)
            String shippingMethod,

            @Size(max = 2000)
            String notes
    ) {
    }

    public record Line(
            @NotBlank(message = "productId is required")
            String productId,

            @Min(value = 1, message = "quantity must be at least 1")
            int quantity
    ) {
    }

    public record Address(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String address,
            @NotBlank String city,
            String state,
            @NotBlank String zipCode,
            @NotBlank String country,
            String phoneNumber
    ) {
    }

    public record PaymentMethod(
            @NotBlank(message = "A payment method type is required")
            String type
            // Card details are deliberately not accepted. Taking a PAN would drag
            // this service into PCI scope for no benefit; a real integration hands
            // the browser off to a payment provider and this service only ever sees
            // the provider's token.
    ) {
    }

    public record OrderItemView(
            String id,
            String productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }

    public record OrderView(
            String id,
            String userId,
            String status,
            String currency,
            BigDecimal subtotal,
            BigDecimal shippingCost,
            BigDecimal total,
            Address shippingAddress,
            String paymentMethod,
            String shippingMethod,
            String notes,
            String createdAt,
            String updatedAt,
            List<OrderItemView> items
    ) {
    }

    public record OrderListView(
            List<OrderView> orders,
            long totalCount,
            int totalPages,
            int currentPage,
            boolean hasNextPage,
            boolean hasPreviousPage
    ) {
    }
}
