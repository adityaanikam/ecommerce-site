package com.ecommerce.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The one synchronous call order-service makes.
 *
 * Why synchronous: the price and the stock have to be known before the order can be
 * written, because the browser's basket carries prices that the user could have
 * edited and a stock figure that may be stale. There is no version of "place the
 * order now, find out the price later" that is acceptable, so this is a blocking
 * call and the order fails if it cannot be made.
 *
 * Why it is wrapped: a blocking call to another service is the classic way one
 * service's slowness becomes everybody's outage. Three things guard it, each doing a
 * different job:
 *
 *   - timeouts, so a catalog that never answers costs two seconds rather than a
 *     request thread for ever;
 *   - a retry, for the single dropped connection that would otherwise fail a
 *     perfectly good order;
 *   - a circuit breaker, so once catalog is properly down, calls fail instantly
 *     instead of every order request queueing on a two second timeout and
 *     exhausting the thread pool.
 *
 * The order of the annotations matters. Resilience4j applies Retry outside
 * CircuitBreaker by default, which would mean retries keep hammering a service the
 * breaker has already given up on, so the breaker is given the higher precedence
 * here and the retry happens inside it.
 */
@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);
    public static final String CIRCUIT_BREAKER = "catalog";

    private final RestClient restClient;

    public CatalogClient(RestClient catalogRestClient) {
        this.restClient = catalogRestClient;
    }

    /**
     * @return the catalog's own price and stock for each id that exists, keyed by
     *         product id. Ids the catalog does not know are simply absent.
     * @throws CatalogUnavailableException when the catalog could not be reached, or
     *         when the breaker is open
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "unavailable")
    @Retry(name = CIRCUIT_BREAKER)
    public Map<String, StockView> lookup(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<StockView> views = restClient.post()
                .uri("/internal/stock/lookup")
                .body(new LookupRequest(productIds))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<StockView>>() {
                });

        if (views == null) {
            return Map.of();
        }
        return views.stream().collect(Collectors.toMap(StockView::productId, Function.identity(),
                (a, b) -> a));
    }

    /**
     * Resilience4j calls this when the breaker is open or the call failed after its
     * retries. The signature has to match the guarded method plus the Throwable.
     */
    @SuppressWarnings("unused")
    private Map<String, StockView> unavailable(List<String> productIds, Throwable cause) {
        // The cause is logged with its stack at debug, because "catalog is
        // unavailable" on its own does not tell an operator whether the catalog is
        // down, slow, or returning something this client cannot parse.
        log.warn("catalog-service is unavailable for {} product(s): {}",
                productIds == null ? 0 : productIds.size(), cause.toString());
        log.debug("catalog-service failure detail", cause);
        throw new CatalogUnavailableException(
                "The catalog is temporarily unavailable, so the order could not be priced", cause);
    }

    public record LookupRequest(List<String> productIds) {
    }

    /**
     * A local copy of catalog's response shape, not an import of catalog's class.
     * Only the fields this service needs are declared, and unknown ones are ignored,
     * so catalog can add to its response without breaking this.
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record StockView(String productId, String name, double price, double effectivePrice, int stock) {
    }
}
