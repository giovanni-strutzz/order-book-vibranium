package com.br.strutz.order_book.adapter.input.rest.mapper;

import com.br.strutz.order_book.adapter.input.rest.dto.response.TradeHistoryResponse;
import com.br.strutz.order_book.application.query.TradeHistoryResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradeRestMapper {

    public TradeHistoryResponse toResponse(TradeHistoryResult result) {
        List<TradeHistoryResponse.TradeEntryResponse> entries = result.getTrades()
                .stream()
                .map(entry -> TradeHistoryResponse.TradeEntryResponse.builder()
                        .tradeId(entry.getTradeId())
                        .side(entry.getSide())
                        .price(entry.getPrice())
                        .quantity(entry.getQuantity())
                        .totalValue(entry.getTotalValue())
                        .executedAt(entry.getExecutedAt())
                        .correlationId(entry.getCorrelationId())
                        .build())
                .toList();

        return TradeHistoryResponse.builder()
                .trades(entries)
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .build();
    }
}