package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.security.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Authenticates the caller once, here, so no downstream service has to.
 *
 * What it does:
 *  - lets public routes through untouched (auth endpoints, catalog reads, images,
 *    actuator, and CORS preflight);
 *  - on every other route, requires a valid Bearer token and answers 401 without
 *    forwarding anything if it is missing, malformed, expired or badly signed;
 *  - on success, strips any client-supplied identity headers and sets its own
 *    X-User-Id, X-User-Email and X-User-Roles from the verified claims.
 *
 * That last point is the one that matters for security: a downstream service trusts
 * X-User-Id, so a client must never be able to send it. The headers are overwritten
 * unconditionally on authenticated routes and removed on public ones, so a request
 * that arrives claiming to be user 1 cannot keep that claim.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** Open to anyone, with any method. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/api/images/**",
            "/actuator/**"
    );

    /** Open to anyone, but only for reads. A write still needs a token. */
    private static final List<String> PUBLIC_READ_PATHS = List.of(
            "/api/products/**",
            "/api/categories/**"
    );

    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // The browser sends preflight without an Authorization header by design.
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        if (isPublic(path, request.getMethod())) {
            // Even a public route must not carry a client's own identity headers
            // through to a service that would trust them.
            return chain.filter(exchange.mutate().request(stripIdentityHeaders(request)).build());
        }

        Optional<JwtVerifier.AuthenticatedUser> user = extractToken(request).flatMap(jwtVerifier::verify);
        if (user.isEmpty()) {
            log.debug("Rejecting unauthenticated request to {}", path);
            return unauthorized(exchange);
        }

        JwtVerifier.AuthenticatedUser authenticated = user.get();
        ServerHttpRequest mutated = stripIdentityHeaders(request).mutate()
                .header(USER_ID_HEADER, authenticated.userId())
                .header(USER_ROLES_HEADER, String.join(",", authenticated.roles()))
                .header(USER_EMAIL_HEADER, authenticated.email() == null ? "" : authenticated.email())
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private static ServerHttpRequest stripIdentityHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_EMAIL_HEADER);
                    headers.remove(USER_ROLES_HEADER);
                })
                .build();
    }

    private static boolean isPublic(String path, HttpMethod method) {
        for (String pattern : PUBLIC_PATHS) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        if (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method)) {
            for (String pattern : PUBLIC_READ_PATHS) {
                if (MATCHER.match(pattern, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Optional<String> extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(BEARER_PREFIX.length()).trim());
    }

    private static Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String correlationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGatewayFilter.HEADER);
        String body = "{\"error\":\"unauthorized\",\"message\":\"A valid Bearer token is required\""
                + (correlationId == null ? "" : ",\"correlationId\":\"" + correlationId + "\"")
                + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // After the correlation id filter, before routing.
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
