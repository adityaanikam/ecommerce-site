package com.ecommerce.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * The one and only place CORS is configured.
 *
 * In the monolith the allowed origins were declared in five places: once in
 * WebConfig from a property, and four more times hardcoded in @CrossOrigin on the
 * controllers, one of which used "*". Every service behind this gateway now has no
 * CORS configuration at all, because every browser request arrives here first.
 *
 * Origins come from GATEWAY_ALLOWED_ORIGINS, comma separated. There is no wildcard
 * default: an unset variable allows localhost only, so a misconfigured deployment
 * fails closed rather than opening the API to every origin on the internet.
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Value("${gateway.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${gateway.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Value("${gateway.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsWebFilter corsWebFilter() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With",
                "Idempotency-Key", "X-Correlation-Id"));
        // The frontend needs to be able to read the correlation id back off a
        // response so a user can quote it in a bug report.
        config.setExposedHeaders(List.of("X-Correlation-Id"));
        config.setMaxAge(maxAge);
        config.setAllowCredentials(allowCredentials);

        if (origins.contains("*")) {
            // Credentials plus a wildcard origin is rejected by browsers anyway, so
            // allow the pattern form and refuse to also send credentials.
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(false);
            log.warn("CORS is configured with a wildcard origin. Credentials are disabled as a result.");
        } else {
            config.setAllowedOrigins(origins);
        }
        log.info("CORS allowed origins: {} (credentials: {})", origins, config.getAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
