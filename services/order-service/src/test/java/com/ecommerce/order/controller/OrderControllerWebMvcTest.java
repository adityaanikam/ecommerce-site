package com.ecommerce.order.controller;

import com.ecommerce.order.client.CatalogUnavailableException;
import com.ecommerce.order.dto.OrderDtos;
import com.ecommerce.order.service.OrderException;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.web.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private static final String USER_ID = "11111111-2222-3333-4444-555555555555";

    private static final String VALID_BODY = """
            {
              "items": [{"productId": "ELEC001", "quantity": 2}],
              "shippingAddress": {
                "firstName": "Ada", "lastName": "Lovelace", "address": "1 Main St",
                "city": "London", "zipCode": "SW1A 1AA", "country": "GB"
              },
              "paymentMethod": {"type": "CARD"},
              "shippingMethod": "STANDARD"
            }
            """;

    private static OrderDtos.OrderView view() {
        return new OrderDtos.OrderView(
                UUID.randomUUID().toString(), USER_ID, "PENDING", "USD",
                new BigDecimal("1999.98"), BigDecimal.ZERO, new BigDecimal("1999.98"),
                new OrderDtos.Address("Ada", "Lovelace", "1 Main St", "London", null,
                        "SW1A 1AA", "GB", null),
                "CARD", "STANDARD", null, "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z",
                List.of());
    }

    @Test
    @DisplayName("placing an order returns 201 and the envelope")
    void placeOrderReturns201() throws Exception {
        when(orderService.placeOrder(any(), any(), any())).thenReturn(view());

        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.total").value(1999.98));
    }

    @Test
    @DisplayName("the Idempotency-Key header is passed through as given")
    void idempotencyKeyIsPassedThrough() throws Exception {
        UUID key = UUID.randomUUID();
        when(orderService.placeOrder(any(), any(), any())).thenReturn(view());

        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .header("Idempotency-Key", key.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(orderService).placeOrder(eq(UUID.fromString(USER_ID)), eq(key), any());
    }

    @Test
    @DisplayName("a non-UUID Idempotency-Key is a 400, not a silent new order")
    void malformedIdempotencyKeyIs400() throws Exception {
        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .header("Idempotency-Key", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(), any(), any());
    }

    @Test
    @DisplayName("no X-User-Id means 401, since the gateway should always set it")
    void missingUserIdIs401() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).placeOrder(any(), any(), any());
    }

    @Test
    @DisplayName("an empty basket is rejected by validation")
    void emptyBasketIsRejected() throws Exception {
        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [], "shippingAddress": {"firstName":"A","lastName":"B",
                                 "address":"1","city":"L","zipCode":"Z","country":"GB"},
                                 "paymentMethod":{"type":"CARD"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("a zero quantity is rejected by validation")
    void zeroQuantityIsRejected() throws Exception {
        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId":"ELEC001","quantity":0}],
                                 "shippingAddress": {"firstName":"A","lastName":"B",
                                 "address":"1","city":"L","zipCode":"Z","country":"GB"},
                                 "paymentMethod":{"type":"CARD"}}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("an unavailable catalog is 503 with Retry-After, not 500")
    void catalogDownIs503() throws Exception {
        when(orderService.placeOrder(any(), any(), any()))
                .thenThrow(new CatalogUnavailableException("catalog down", new RuntimeException()));

        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "10"));
    }

    @Test
    @DisplayName("insufficient stock is a 409 with a message naming the product")
    void outOfStockIs409() throws Exception {
        when(orderService.placeOrder(any(), any(), any()))
                .thenThrow(OrderException.outOfStock("iPhone", 5, 2));

        mockMvc.perform(post("/orders")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Not enough stock for iPhone: asked for 5, only 2 left"));
    }

    @Test
    @DisplayName("fetching another user's order id is a 404, not a leak")
    void unknownOrderIs404() throws Exception {
        when(orderService.findForUser(any(), any())).thenThrow(OrderException.orderNotFound());

        mockMvc.perform(get("/orders/" + UUID.randomUUID()).header("X-User-Id", USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("listing orders returns the paging shape the frontend expects")
    void listReturnsPagingShape() throws Exception {
        when(orderService.listForUser(any(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(new OrderDtos.OrderListView(List.of(view()), 1, 1, 0, false, false));

        mockMvc.perform(get("/orders/user").header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orders").isArray())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false));
    }
}
