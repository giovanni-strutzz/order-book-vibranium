package com.br.strutz.order_book.adapter.output.mongo.mapper;

import com.br.strutz.order_book.adapter.output.mongo.document.TradeDocument;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.TradeId;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class TradeMapper {

    public TradeDocument toDocument(Trade trade) {
        return TradeDocument.builder()
                .id(trade.getId().getValue())
                .buyOrderId(trade.getBuyOrderId().getValue())
                .sellOrderId(trade.getSellOrderId().getValue())
                .buyerId(trade.getBuyerId().getValue())
                .sellerId(trade.getSellerId().getValue())
                .price(trade.getPrice().getAmount())
                .quantity(trade.getQuantity().getAmount())
                .totalValue(trade.getTotalValue().getAmount())
                .executedAt(trade.getExecutedAt())
                .correlationId(trade.getCorrelationId())
                .build();
    }

    public Trade toDomain(TradeDocument doc) {
        return Trade.builder()
                .id(TradeId.of(doc.getId()))
                .buyOrderId(OrderId.of(doc.getBuyOrderId()))
                .sellOrderId(OrderId.of(doc.getSellOrderId()))
                .buyerId(UserId.of(doc.getBuyerId()))
                .sellerId(UserId.of(doc.getSellerId()))
                .price(Money.of(doc.getPrice()))
                .quantity(Money.of(doc.getQuantity()))
                .totalValue(Money.of(doc.getTotalValue()))
                .executedAt(doc.getExecutedAt())
                .correlationId(doc.getCorrelationId())
                .build();
    }
}
