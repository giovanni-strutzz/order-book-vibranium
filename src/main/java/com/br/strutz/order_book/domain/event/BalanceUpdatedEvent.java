package com.br.strutz.order_book.domain.event;

import com.br.strutz.order_book.domain.model.aggregates.Trade;

public class BalanceUpdatedEvent extends Event {

    private final String tradeId;
    private final String buyerId;
    private final String sellerId;
    private final String totalValue;
    private final String quantity;

    public BalanceUpdatedEvent(Trade trade) {
        super(trade.getCorrelationId(), trade.getBuyerId().getValue());
        this.tradeId    = trade.getId().getValue();
        this.buyerId    = trade.getBuyerId().getValue();
        this.sellerId   = trade.getSellerId().getValue();
        this.totalValue = trade.getTotalValue().getAmount().toPlainString();
        this.quantity   = trade.getQuantity().getAmount().toPlainString();
    }
}
