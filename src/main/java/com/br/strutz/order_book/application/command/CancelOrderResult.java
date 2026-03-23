package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.order.OrderId;
import lombok.Value;

@Value
public class CancelOrderResult {
    OrderId orderId;
    String  status;
    String  message;

    public static CancelOrderResult success(OrderId orderId) {
        return new CancelOrderResult(orderId, "CANCELLED", "Order cancelled successfully");
    }

    public static CancelOrderResult notFound(OrderId orderId) {
        return new CancelOrderResult(orderId, "NOT_FOUND", "Not found Order");
    }
}
