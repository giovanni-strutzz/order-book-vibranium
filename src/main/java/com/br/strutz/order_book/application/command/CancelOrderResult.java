package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Value;

@Value
public class CancelOrderResult {
    OrderId orderId;
    String status;
    String message;
    UserId userId;

    public static CancelOrderResult success(OrderId orderId, UserId userId) {
        return new CancelOrderResult(orderId, "CANCELLED", "Order cancelled successfully", userId);
    }

    public static CancelOrderResult notFound(OrderId orderId, UserId userId) {
        return new CancelOrderResult(orderId, "NOT_FOUND", "Not found Order", userId);
    }
}
