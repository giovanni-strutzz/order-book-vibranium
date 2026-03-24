package com.br.strutz.order_book.domain.exception;

import com.br.strutz.order_book.domain.model.order.OrderId;

public class OrderNotFoundException extends DomainException {

    public OrderNotFoundException(OrderId orderId) {
        super("Not Found Order: " + orderId.getValue());
    }
}