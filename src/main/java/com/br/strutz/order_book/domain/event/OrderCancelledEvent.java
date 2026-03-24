package com.br.strutz.order_book.domain.event;

import com.br.strutz.order_book.domain.model.aggregates.Order;
import lombok.Getter;

@Getter
public class OrderCancelledEvent extends Event {

    private final String orderId;
    private final String orderType;
    private final String remainingQuantity;

    public OrderCancelledEvent(Order order) {
        super(order.getCorrelationId(), order.getUserId().getValue());
        this.orderId           = order.getId().getValue();
        this.orderType         = order.getType().name();
        this.remainingQuantity = order.remainingQuantity().getAmount().toPlainString();
    }
}
