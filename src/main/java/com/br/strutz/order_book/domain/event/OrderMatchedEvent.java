package com.br.strutz.order_book.domain.event;

import com.br.strutz.order_book.domain.model.aggregates.Trade;
import lombok.Getter;

@Getter
public class OrderMatchedEvent extends Event {

    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String price;
    private final String quantity;
    private final String totalValue;

    public OrderMatchedEvent(Trade trade) {
        super(trade.getCorrelationId(), trade.getBuyerId().getValue());
        this.tradeId     = trade.getId().getValue();
        this.buyOrderId  = trade.getBuyOrderId().getValue();
        this.sellOrderId = trade.getSellOrderId().getValue();
        this.price       = trade.getPrice().getAmount().toPlainString();
        this.quantity    = trade.getQuantity().getAmount().toPlainString();
        this.totalValue  = trade.getTotalValue().getAmount().toPlainString();
    }
}