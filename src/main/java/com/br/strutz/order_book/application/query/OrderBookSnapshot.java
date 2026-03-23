package com.br.strutz.order_book.application.query;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class OrderBookSnapshot {

    List<PriceLevel> bids;
    List<PriceLevel> asks;
    BigDecimal spread;
    Instant generatedAt;

    public static OrderBookSnapshot empty() {
        return OrderBookSnapshot.builder()
                .bids(List.of())
                .asks(List.of())
                .spread(BigDecimal.ZERO)
                .generatedAt(Instant.now())
                .build();
    }


    @Value
    @Builder
    public static class PriceLevel {
        BigDecimal price;
        BigDecimal quantity;
        int orderCount;
    }
}
