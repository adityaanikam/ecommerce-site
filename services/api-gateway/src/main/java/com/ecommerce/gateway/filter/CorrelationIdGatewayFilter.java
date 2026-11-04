package com.ecommerce.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Stamps every request with a correlation id and makes sure it is forwarded.
 *
 * This runs before everything else so that even a request that is rejected for a bad
 * token is logged under an id the caller can quote. An id supplied by the caller is
 * honoured, which is what lets the frontend tie a browser session to server logs.
 *
 * It is also written back on the response, so the browser and anyone reading a HAR
 * file can see it.
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        final String id = correlationId;

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(HEADER, id)
                .build();
        exchange.getResponse().getHeaders().set(HEADER, id);

        MDC.put(MDC_KEY, id);
        try {
            return chain.filter(exchange.mutate().request(mutated).build());
        } finally {
            // WebFlux hands the rest of the work to other threads, so the MDC is only
            // reliable for logging done synchronously in the filter chain. Downstream
            // services put the forwarded header back into their own MDC, which is
            // where the per-request logging actually happens.
            MDC.remove(MDC_KEY);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
