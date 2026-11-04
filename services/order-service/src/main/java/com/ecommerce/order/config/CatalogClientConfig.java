package com.ecommerce.order.config;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class CatalogClientConfig {

    @Bean
    public RestClient catalogRestClient(
            @Value("${order.catalog.base-url:http://localhost:8081}") String baseUrl,
            @Value("${order.catalog.connect-timeout-ms:1000}") long connectTimeoutMs,
            @Value("${order.catalog.read-timeout-ms:2000}") long readTimeoutMs) {

        // Explicit timeouts. The JDK default is no timeout at all, which is how one
        // slow dependency turns into every request thread blocked for ever.
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .withReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(ClientHttpRequestFactories.get(settings))
                // Carry the correlation id across the service boundary, so one
                // request can be followed through the gateway, order-service and
                // catalog-service in the logs.
                .requestInterceptor((request, body, execution) -> {
                    String correlationId = MDC.get("correlationId");
                    if (correlationId != null) {
                        request.getHeaders().add("X-Correlation-Id", correlationId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
