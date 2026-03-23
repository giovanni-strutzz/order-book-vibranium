package com.br.strutz.order_book.application.query;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class TradeHistoryResult {

    List<TradeEntry> trades;
    int page;
    int size;
    long totalElements;


    @Value
    @Builder
    public static class TradeEntry {

        String tradeId;
        String side;
        BigDecimal price;
        BigDecimal quantity;
        BigDecimal totalValue;
        Instant executedAt;
        String correlationId;
    }
}
