package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Value;

@Value
public class PlaceOrderResult {

    OrderId orderId;
    UserId userId;
    String status;
    String message;

    public static PlaceOrderResult success(OrderId orderId, UserId userId) {
        return new PlaceOrderResult(orderId, userId, "ACCEPTED",
                "Order successfully received");
    }

    public static PlaceOrderResult rejected(String reason) {
        return new PlaceOrderResult(null, null, "REJECTED", reason);
    }
}
