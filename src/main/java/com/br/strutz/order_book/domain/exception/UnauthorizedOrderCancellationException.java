package com.br.strutz.order_book.domain.exception;

import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.user.UserId;

public class UnauthorizedOrderCancellationException extends DomainException {

    public UnauthorizedOrderCancellationException(OrderId orderId, UserId userId) {
        super("User does not have permission to cancel order %s"
                .formatted(userId.getValue(), orderId.getValue()));
    }
}