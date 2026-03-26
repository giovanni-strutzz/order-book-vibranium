package com.br.strutz.order_book.application.query.handler;

import com.br.strutz.order_book.application.query.GetTradeHistoryQuery;
import com.br.strutz.order_book.application.query.TradeHistoryResult;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.port.input.GetTradeHistoryUseCase;
import com.br.strutz.order_book.domain.port.output.trade.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTradeHistoryQueryHandler implements GetTradeHistoryUseCase {

    private final TradeRepository tradeRepository;

    public GetTradeHistoryQueryHandler(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public TradeHistoryResult handle(GetTradeHistoryQuery query) {
        List<Trade> trades = tradeRepository.findByUserIdOrderByExecutedAtDesc(
                query.getUserId(),
                query.getPage(),
                query.getSize());

        List<TradeHistoryResult.TradeEntry> entries = trades.stream()
                .map(trade -> toEntry(trade, query.getUserId().getValue()))
                .toList();

        return TradeHistoryResult.builder()
                .trades(entries)
                .page(query.getPage())
                .size(query.getSize())
                .totalElements(entries.size())
                .build();
    }

    private TradeHistoryResult.TradeEntry toEntry(Trade trade, String userId) {
        String side = trade.getBuyerId().getValue().equals(userId)
                ? "BUY"
                : "SELL";

        return TradeHistoryResult.TradeEntry.builder()
                .tradeId(trade.getId().getValue())
                .side(side)
                .price(trade.getPrice().getAmount())
                .quantity(trade.getQuantity().getAmount())
                .totalValue(trade.getTotalValue().getAmount())
                .executedAt(trade.getExecutedAt())
                .correlationId(trade.getCorrelationId())
                .build();
    }
}
