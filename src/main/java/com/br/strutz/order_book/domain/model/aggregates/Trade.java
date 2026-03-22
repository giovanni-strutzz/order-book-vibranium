package com.br.strutz.order_book.domain.model.aggregates;

import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.TradeId;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class Trade {

    TradeId id;
    OrderId buyOrderId;
    OrderId sellOrderId;
    UserId buyerId;
    UserId sellerId;
    Money price;
    Money quantity;
    Money totalValue;
    Instant executedAt;
    String correlationId;


    public static Trade execute(Order buyOrder, Order sellOrder, Money quantity) {
        var price = sellOrder.getPrice();
        var totalValue = price.multiply(quantity);

        return Trade.builder()
                .id(TradeId.generate())
                .buyOrderId(buyOrder.getId())
                .sellOrderId(sellOrder.getId())
                .buyerId(buyOrder.getUserId())
                .sellerId(sellOrder.getUserId())
                .price(price)
                .quantity(quantity)
                .totalValue(totalValue)
                .executedAt(Instant.now())
                .correlationId(buyOrder.getCorrelationId())
                .build();
    }
}
