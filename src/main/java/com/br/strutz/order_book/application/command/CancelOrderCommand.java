package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CancelOrderCommand {

    OrderId orderId;
    UserId userId;
    String correlationId;
}
