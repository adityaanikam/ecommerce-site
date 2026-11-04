package com.ecommerce.catalog.messaging;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The hard part this proves: a duplicate OrderPlaced decrements stock exactly once.
 */
@ExtendWith(MockitoExtension.class)
class OrderPlacedConsumerTest {

    @Mock
    private ProcessedEventRepository processedEvents;

    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderPlacedConsumer consumer;

    private static OrderPlacedEvent event(String eventId) {
        return new OrderPlacedEvent(eventId, "order-1",
                List.of(new OrderPlacedEvent.Line("ELEC001", 2),
                        new OrderPlacedEvent.Line("ELEC002", 1)));
    }

    @Test
    @DisplayName("the same OrderPlaced delivered twice decrements stock once")
    void duplicateEventDecrementsOnce() {
        OrderPlacedEvent duplicate = event("evt-1");

        // First delivery claims the event id; the second collides on the primary key,
        // which is exactly what Kafka at-least-once delivery looks like in practice.
        when(processedEvents.insert(any(ProcessedEvent.class)))
                .thenReturn(new ProcessedEvent("evt-1", "OrderPlaced", null))
                .thenThrow(new DuplicateKeyException("_id already present"));
        when(stockService.decrementStock(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(Product.builder().id("ELEC001").stock(1).build());

        consumer.handle(duplicate);
        consumer.handle(duplicate);

        verify(stockService, times(1)).decrementStock("ELEC001", 2);
        verify(stockService, times(1)).decrementStock("ELEC002", 1);
    }

    @Test
    @DisplayName("a first delivery decrements every line")
    void firstDeliveryDecrementsEveryLine() {
        when(processedEvents.insert(any(ProcessedEvent.class)))
                .thenReturn(new ProcessedEvent("evt-2", "OrderPlaced", null));
        when(stockService.decrementStock(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(Product.builder().id("x").stock(0).build());

        consumer.handle(event("evt-2"));

        verify(stockService).decrementStock("ELEC001", 2);
        verify(stockService).decrementStock("ELEC002", 1);
    }

    @Test
    @DisplayName("an event with no id is dropped rather than applied undeduplicatably")
    void eventWithoutIdIsDropped() {
        consumer.handle(new OrderPlacedEvent(null, "order-1",
                List.of(new OrderPlacedEvent.Line("ELEC001", 1))));

        verifyNoInteractions(stockService);
        verifyNoInteractions(processedEvents);
    }

    @Test
    @DisplayName("a malformed line is skipped without failing the whole event")
    void malformedLineIsSkipped() {
        when(processedEvents.insert(any(ProcessedEvent.class)))
                .thenReturn(new ProcessedEvent("evt-3", "OrderPlaced", null));
        when(stockService.decrementStock(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(Product.builder().id("ELEC002").stock(3).build());

        consumer.handle(new OrderPlacedEvent("evt-3", "order-2", List.of(
                new OrderPlacedEvent.Line(null, 2),
                new OrderPlacedEvent.Line("ELEC002", 0),
                new OrderPlacedEvent.Line("ELEC002", 4))));

        verify(stockService, never()).decrementStock(null, 2);
        verify(stockService, never()).decrementStock("ELEC002", 0);
        verify(stockService).decrementStock("ELEC002", 4);
    }

    @Test
    @DisplayName("an oversell is logged but does not stop the remaining lines")
    void oversellDoesNotStopRemainingLines() {
        when(processedEvents.insert(any(ProcessedEvent.class)))
                .thenReturn(new ProcessedEvent("evt-4", "OrderPlaced", null));
        when(stockService.decrementStock("ELEC001", 2)).thenReturn(null);
        when(stockService.decrementStock("ELEC002", 1))
                .thenReturn(Product.builder().id("ELEC002").stock(5).build());

        consumer.handle(event("evt-4"));

        verify(stockService).decrementStock("ELEC001", 2);
        verify(stockService).decrementStock("ELEC002", 1);
    }
}
