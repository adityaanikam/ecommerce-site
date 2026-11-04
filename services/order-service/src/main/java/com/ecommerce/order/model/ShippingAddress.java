package com.ecommerce.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embedded rather than a separate table: an order's shipping address is part of the
 * order, is never shared, and is never updated after the fact.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @Column(name = "shipping_first_name", nullable = false)
    private String firstName;

    @Column(name = "shipping_last_name", nullable = false)
    private String lastName;

    @Column(name = "shipping_address", nullable = false)
    private String address;

    @Column(name = "shipping_city", nullable = false)
    private String city;

    @Column(name = "shipping_state")
    private String state;

    @Column(name = "shipping_zip_code", nullable = false)
    private String zipCode;

    @Column(name = "shipping_country", nullable = false)
    private String country;

    @Column(name = "shipping_phone")
    private String phoneNumber;
}
