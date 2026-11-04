package com.ecommerce.order.client;

/**
 * The catalog could not be consulted, so the order cannot be priced and must not be
 * accepted. Surfaced as a 503 with a Retry-After, because retrying later is exactly
 * the right thing for the client to do.
 */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
