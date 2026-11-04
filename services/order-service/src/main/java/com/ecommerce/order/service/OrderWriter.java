package com.ecommerce.order.service;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.dto.OrderDtos;
import com.ecommerce.order.model.IdempotencyRecord;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.ShippingAddress;
import com.ecommerce.order.outbox.OrderPlacedPayload;
import com.ecommerce.order.outbox.OutboxEvent;
import com.ecommerce.order.outbox.OutboxEventRepository;
import com.ecommerce.order.repository.IdempotencyRecordRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The transactional boundary for placing an order, kept in its own bean on purpose.
 *
 * @Transactional is applied by a Spring proxy, and a call from inside the same bean
 * does not go through that proxy. Had these methods lived on OrderService and been
 * called as this.createOrder(...), the annotation would have done nothing at all and
 * the order, the outbox event and the idempotency record would each have committed
 * separately: exactly the atomicity this design exists to provide, silently absent.
 * Two beans makes the proxy real.
 */
@Component
@RequiredArgsConstructor
public class OrderWriter {

    private static final Logger log = LoggerFactory.getLogger(OrderWriter.class);

    private static final String CURRENCY = "USD";
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    private static final BigDecimal FLAT_SHIPPING = new BigDecimal("4.99");

    private final OrderRepository orders;
    private final IdempotencyRecordRepository idempotencyKeys;
    private final OutboxEventRepository outbox;
    private final ObjectMapper objectMapper;

    /**
     * Has this user already used this key? If so, hand back the order it produced.
     */
    @Transactional(readOnly = true)
    public Optional<OrderDtos.OrderView> replay(UUID userId, UUID idempotencyKey, String requestHash) {
        return idempotencyKeys.findById(new IdempotencyRecord.Key(userId, idempotencyKey))
                .map(record -> {
                    if (!record.getRequestHash().equals(requestHash)) {
                        throw OrderException.idempotencyKeyReused();
                    }
                    return orders.findById(record.getOrderId())
                            .map(OrderWriter::toView)
                            .orElseThrow(OrderException::orderNotFound);
                });
    }

    /**
     * The order, its lines, the OrderPlaced event and the idempotency record, in one
     * transaction. All four commit or none do.
     */
    @Transactional
    public OrderDtos.OrderView createOrder(UUID userId,
                                           UUID idempotencyKey,
                                           String requestHash,
                                           OrderDtos.CreateOrderRequest request,
                                           Map<String, CatalogClient.StockView> catalog) {
        Instant now = Instant.now();
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .currency(CURRENCY)
                .shippingAddress(toEntity(request.shippingAddress()))
                .paymentMethod(request.paymentMethod().type())
                .shippingMethod(request.shippingMethod())
                .notes(request.notes())
                .createdAt(now)
                .updatedAt(now)
                .subtotal(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDtos.Line line : request.items()) {
            CatalogClient.StockView product = catalog.get(line.productId());
            if (product == null) {
                throw OrderException.unknownProduct(line.productId());
            }
            if (product.stock() < line.quantity()) {
                throw OrderException.outOfStock(product.name(), line.quantity(), product.stock());
            }

            // The catalog's price, never the browser's. The basket in localStorage
            // is entirely under the user's control.
            BigDecimal unitPrice = BigDecimal.valueOf(product.effectivePrice())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            order.addItem(OrderItem.builder()
                    .id(UUID.randomUUID())
                    .productId(product.productId())
                    .productName(product.name())
                    .unitPrice(unitPrice)
                    .quantity(line.quantity())
                    .lineTotal(lineTotal)
                    .build());

            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : FLAT_SHIPPING;
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setShippingCost(shipping);
        order.setTotal(subtotal.add(shipping).setScale(2, RoundingMode.HALF_UP));

        orders.save(order);

        // Same transaction as the order. This is the outbox: the event cannot be
        // published to Kafka here, because a database and a broker cannot commit
        // together, so it is written to a table that commits with the order and
        // OutboxPublisher sends it from there.
        outbox.save(buildOrderPlacedEvent(order, now));

        idempotencyKeys.save(IdempotencyRecord.builder()
                .id(new IdempotencyRecord.Key(userId, idempotencyKey))
                .requestHash(requestHash)
                .orderId(orderId)
                .createdAt(now)
                .build());

        log.info("Placed order {} for user {}, {} line(s), total {}",
                orderId, userId, order.getItems().size(), order.getTotal());
        return toView(order);
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderView findForUser(UUID userId, UUID orderId) {
        return orders.findByIdAndUserId(orderId, userId)
                .map(OrderWriter::toView)
                .orElseThrow(OrderException::orderNotFound);
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderListView listForUser(UUID userId, int page, int size) {
        var found = orders.findByUserIdOrderByCreatedAtDesc(userId,
                org.springframework.data.domain.PageRequest.of(page, size));
        return new OrderDtos.OrderListView(
                found.getContent().stream().map(OrderWriter::toView).toList(),
                found.getTotalElements(),
                found.getTotalPages(),
                found.getNumber(),
                found.hasNext(),
                found.hasPrevious());
    }

    private OutboxEvent buildOrderPlacedEvent(Order order, Instant now) {
        // The event id is generated here, inside the transaction, and is also the
        // outbox row's primary key. A republish after a crash therefore carries the
        // same id, which is what lets catalog-service recognise the duplicate.
        UUID eventId = UUID.randomUUID();
        OrderPlacedPayload payload = new OrderPlacedPayload(
                eventId.toString(),
                order.getId().toString(),
                order.getUserId().toString(),
                now.toString(),
                order.getItems().stream()
                        .map(i -> new OrderPlacedPayload.Line(i.getProductId(), i.getQuantity()))
                        .toList());

        return OutboxEvent.builder()
                .id(eventId)
                .aggregateType("Order")
                .aggregateId(order.getId().toString())
                .eventType("OrderPlaced")
                .payload(serialise(payload))
                .correlationId(MDC.get("correlationId"))
                .createdAt(now)
                .attempts(0)
                .build();
    }

    private static ShippingAddress toEntity(OrderDtos.Address address) {
        return ShippingAddress.builder()
                .firstName(address.firstName())
                .lastName(address.lastName())
                .address(address.address())
                .city(address.city())
                .state(address.state())
                .zipCode(address.zipCode())
                .country(address.country())
                .phoneNumber(address.phoneNumber())
                .build();
    }

    static OrderDtos.OrderView toView(Order order) {
        ShippingAddress shipping = order.getShippingAddress();
        return new OrderDtos.OrderView(
                order.getId().toString(),
                order.getUserId().toString(),
                order.getStatus().name(),
                order.getCurrency(),
                order.getSubtotal(),
                order.getShippingCost(),
                order.getTotal(),
                new OrderDtos.Address(
                        shipping.getFirstName(), shipping.getLastName(), shipping.getAddress(),
                        shipping.getCity(), shipping.getState(), shipping.getZipCode(),
                        shipping.getCountry(), shipping.getPhoneNumber()),
                order.getPaymentMethod(),
                order.getShippingMethod(),
                order.getNotes(),
                order.getCreatedAt().toString(),
                order.getUpdatedAt().toString(),
                order.getItems().stream()
                        .map(i -> new OrderDtos.OrderItemView(
                                i.getId().toString(), i.getProductId(), i.getProductName(),
                                i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                        .toList());
    }

    private String serialise(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise the outbox payload", e);
        }
    }
}
