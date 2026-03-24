package com.br.strutz.order_book.domain.event;

import com.br.strutz.order_book.domain.model.aggregates.Order;
import lombok.Getter;

@Getter
public class OrderPlacedEvent extends Event {

    private final String orderId;
    private final String orderType;
    private final String price;
    private final String quantity;

    public OrderPlacedEvent(Order order) {
        super(order.getCorrelationId(), order.getUserId().getValue());
        this.orderId   = order.getId().getValue();
        this.orderType = order.getType().name();
        this.price     = order.getPrice().getAmount().toPlainString();
        this.quantity  = order.getQuantity().getAmount().toPlainString();
    }
}
