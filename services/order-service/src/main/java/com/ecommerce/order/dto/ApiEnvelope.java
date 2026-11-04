package com.ecommerce.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * The {success, data, message} wrapper the frontend already unwraps.
 *
 * Both of the frontend's API layers read response.data.data for the payload and
 * response.data.message for an error string, so the backend has to produce this
 * shape or every existing call site would need rewriting. Matching the client
 * contract is cheaper than changing the client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiEnvelope<T>(boolean success, T data, String message, List<String> errors) {

    public static <T> ApiEnvelope<T> ok(T data) {
        return new ApiEnvelope<>(true, data, null, null);
    }

    public static <T> ApiEnvelope<T> ok(T data, String message) {
        return new ApiEnvelope<>(true, data, message, null);
    }

    public static <T> ApiEnvelope<T> error(String message) {
        return new ApiEnvelope<>(false, null, message, null);
    }

    public static <T> ApiEnvelope<T> error(String message, List<String> errors) {
        return new ApiEnvelope<>(false, null, message, errors);
    }
}
