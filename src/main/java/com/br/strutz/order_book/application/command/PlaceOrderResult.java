package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.order.OrderId;
import lombok.Value;

@Value
public class PlaceOrderResult {

    OrderId orderId;
    String status;
    String message;

    public static PlaceOrderResult success(OrderId orderId) {
        return new PlaceOrderResult(orderId, "ACCEPTED", "Order successfully received");
    };

    public static PlaceOrderResult rejected(String reason) {
        return new PlaceOrderResult(null, "REJECTED", reason);
    }
}
