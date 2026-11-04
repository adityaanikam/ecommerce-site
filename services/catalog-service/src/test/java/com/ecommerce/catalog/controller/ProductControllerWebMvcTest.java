package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the HTTP layer. The important assertions are about the wire shape,
 * because the live frontend already parses it: the three boolean flags must still
 * serialise as "new", "featured" and "onSale", and the list endpoint must still
 * return a Spring Data Page with a "content" array.
 */
@WebMvcTest(controllers = ProductController.class)
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private static Product sample() {
        return Product.builder()
                .id("ELEC001")
                .name("iPhone 15 Pro")
                .brand("Apple")
                .price(999.99)
                .discountPrice(0.0)
                .category("Electronics")
                .subcategory("Mobiles")
                .images(List.of("https://placehold.co/800x800/6366f1/ffffff"))
                .description("Latest iPhone")
                .stock(50)
                .rating(4.8)
                .specs(Map.of("Storage", "256GB"))
                .slug("elec001")
                .isNew(true)
                .isFeatured(true)
                .isOnSale(false)
                .build();
    }

    @Test
    @DisplayName("GET /products returns a page and keeps the monolith's JSON field names")
    void listReturnsPageWithLegacyFieldNames() throws Exception {
        when(productService.findProducts(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(sample()), PageRequest.of(0, 12), 1));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("ELEC001"))
                .andExpect(jsonPath("$.content[0].stock").value(50))
                // These three names are what the frontend reads. Renaming the fields
                // would silently break the badges in the UI.
                .andExpect(jsonPath("$.content[0].new").value(true))
                .andExpect(jsonPath("$.content[0].featured").value(true))
                .andExpect(jsonPath("$.content[0].onSale").value(false))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /products passes every filter through to the service")
    void filtersArePassedThrough() throws Exception {
        when(productService.findProducts(eq("Electronics"), eq("Mobiles"), isNull(),
                eq(100.0), eq(900.0), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        mockMvc.perform(get("/products")
                        .param("page", "2")
                        .param("size", "5")
                        .param("category", "Electronics")
                        .param("subcategory", "Mobiles")
                        .param("minPrice", "100.0")
                        .param("maxPrice", "900.0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /products/{id} returns 404 for an unknown id")
    void unknownProductIsNotFound() throws Exception {
        when(productService.findById("nope")).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/nope")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /products/{id} returns the product for a known id")
    void knownProductIsReturned() throws Exception {
        when(productService.findById("ELEC001")).thenReturn(Optional.of(sample()));

        mockMvc.perform(get("/products/ELEC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("GET /products/featured is not swallowed by the {id} mapping")
    void featuredIsNotTreatedAsAnId() throws Exception {
        when(productService.findFeaturedProducts()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("ELEC001"));
    }

    @Test
    @DisplayName("no CORS header is produced by the service itself, the gateway owns CORS")
    void serviceDoesNotSetCorsHeaders() throws Exception {
        when(productService.findProducts(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 12), 0));

        mockMvc.perform(get("/products").header("Origin", "http://evil.example"))
                .andExpect(status().isOk())
                .andExpect(result ->
                        org.assertj.core.api.Assertions
                                .assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
                                .isNull());
    }
}
