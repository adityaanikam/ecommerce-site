package com.ecommerce.order.service;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.dto.OrderDtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderWriter writer;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private OrderService orderService;

    private static final UUID USER = UUID.randomUUID();
    private static final UUID KEY = UUID.randomUUID();

    static OrderDtos.CreateOrderRequest request(OrderDtos.Line... lines) {
        return new OrderDtos.CreateOrderRequest(
                List.of(lines),
                new OrderDtos.Address("Ada", "Lovelace", "1 Main St", "London",
                        null, "SW1A 1AA", "GB", "+447700900123"),
                null,
                new OrderDtos.PaymentMethod("CARD"),
                "STANDARD",
                null);
    }

    private static OrderDtos.OrderView view(String id) {
        return new OrderDtos.OrderView(id, USER.toString(), "PENDING", "USD",
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, null, "CARD", "STANDARD",
                null, "now", "now", List.of());
    }

    @Test
    @DisplayName("the same Idempotency-Key twice creates one order and returns the first one")
    void sameKeyTwiceCreatesOneOrder() {
        OrderDtos.CreateOrderRequest body = request(new OrderDtos.Line("ELEC001", 2));
        OrderDtos.OrderView created = view("order-1");

        when(catalogClient.lookup(anyList())).thenReturn(
                Map.of("ELEC001", new CatalogClient.StockView("ELEC001", "iPhone", 999.0, 999.0, 10)));
        // First call: no record yet. Second call: the record from the first.
        when(writer.replay(eq(USER), eq(KEY), any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(created));
        when(writer.createOrder(eq(USER), eq(KEY), any(), any(), any())).thenReturn(created);

        OrderDtos.OrderView first = orderService.placeOrder(USER, KEY, body);
        OrderDtos.OrderView second = orderService.placeOrder(USER, KEY, body);

        assertThat(first.id()).isEqualTo("order-1");
        assertThat(second.id()).isEqualTo("order-1");
        // The crucial assertion: only one order was ever written.
        verify(writer, times(1)).createOrder(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("a replayed key does not even call the catalog again")
    void replayDoesNotCallCatalog() {
        when(writer.replay(any(), any(), any())).thenReturn(Optional.of(view("order-1")));

        orderService.placeOrder(USER, KEY, request(new OrderDtos.Line("ELEC001", 1)));

        verifyNoInteractions(catalogClient);
        verify(writer, never()).createOrder(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("a concurrent duplicate that loses the primary key race returns the stored order")
    void concurrentDuplicateReturnsStoredOrder() {
        OrderDtos.CreateOrderRequest body = request(new OrderDtos.Line("ELEC001", 1));
        when(catalogClient.lookup(anyList())).thenReturn(
                Map.of("ELEC001", new CatalogClient.StockView("ELEC001", "iPhone", 999.0, 999.0, 10)));
        when(writer.replay(any(), any(), any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(view("order-1")));
        when(writer.createOrder(any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThat(orderService.placeOrder(USER, KEY, body).id()).isEqualTo("order-1");
    }

    @Test
    @DisplayName("a key reused with a different basket is rejected rather than silently replayed")
    void reusedKeyWithDifferentBodyIsRejected() {
        when(writer.replay(any(), any(), any())).thenThrow(OrderException.idempotencyKeyReused());

        assertThatThrownBy(() -> orderService.placeOrder(USER, KEY,
                request(new OrderDtos.Line("ELEC001", 99))))
                .isInstanceOf(OrderException.class)
                .extracting(e -> ((OrderException) e).getStatus())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("the fingerprint ignores line order but not quantities")
    void fingerprintIsOrderInsensitiveButQuantitySensitive() {
        String a = orderService.fingerprint(request(
                new OrderDtos.Line("ELEC001", 2), new OrderDtos.Line("ELEC002", 1)));
        String b = orderService.fingerprint(request(
                new OrderDtos.Line("ELEC002", 1), new OrderDtos.Line("ELEC001", 2)));
        String c = orderService.fingerprint(request(
                new OrderDtos.Line("ELEC001", 3), new OrderDtos.Line("ELEC002", 1)));

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    @DisplayName("the same product twice in one basket is rejected")
    void duplicateProductLinesRejected() {
        when(writer.replay(any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(USER, KEY,
                request(new OrderDtos.Line("ELEC001", 1), new OrderDtos.Line("ELEC001", 2))))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("more than once");

        verifyNoInteractions(catalogClient);
    }
}
