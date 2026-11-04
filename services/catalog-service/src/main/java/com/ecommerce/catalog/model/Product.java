package com.ecommerce.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Carried over from the monolith unchanged in its wire shape.
 *
 * The three boolean fields are named isNew/isFeatured/isOnSale, which makes Lombok
 * generate isNew()/isFeatured()/isOnSale() and therefore makes Jackson serialise them
 * as "new"/"featured"/"onSale". The monolith did the same thing via hand-written
 * accessors, so the JSON the frontend already parses is byte-for-byte the same shape.
 * Do not rename these fields without changing the frontend.
 *
 * Serializable because instances are cached in Redis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String name;
    private String brand;
    private double price;
    private double discountPrice;
    @Indexed
    private String category;
    @Indexed
    private String subcategory;
    private List<String> images;
    private String description;

    /**
     * Units on hand. Already present in the monolith's model; the catalog service is
     * the only owner of this number and the only writer of it.
     */
    private int stock;

    private double rating;
    private int reviewCount;
    private Map<String, String> specs;
    private List<String> features;
    @Indexed
    private String slug;
    private boolean isNew;
    private boolean isFeatured;
    private boolean isOnSale;
    private String color;
    private String size;
    private String material;
    private long createdAt;
    private long updatedAt;
}
