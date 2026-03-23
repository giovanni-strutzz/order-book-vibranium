package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlaceOrderCommand {

    UserId userId;
    OrderType orderType;
    Money price;
    Money quantity;
    String correlationId;
}
