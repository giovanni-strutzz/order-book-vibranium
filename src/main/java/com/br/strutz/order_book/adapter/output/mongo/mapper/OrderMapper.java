package com.br.strutz.order_book.adapter.output.mongo.mapper;

import com.br.strutz.order_book.adapter.output.mongo.document.OrderDocument;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderStatus;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDocument toDocument(Order order) {
        return OrderDocument.builder()
                .id(order.getId().getValue())
                .version(order.getVersion())
                .userId(order.getUserId().getValue())
                .type(order.getType().name())
                .status(order.getStatus().name())
                .price(order.getPrice().getAmount())
                .quantity(order.getQuantity().getAmount())
                .filledQuantity(order.getFilledQuantity().getAmount())
                .totalValue(order.getPrice().multiply(order.getQuantity()).getAmount())
                .correlationId(order.getCorrelationId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public Order toDomain(OrderDocument doc) {
        return Order.reconstitute(
                OrderId.of(doc.getId()),
                UserId.of(doc.getUserId()),
                OrderType.valueOf(doc.getType()),
                Money.of(doc.getPrice()),
                Money.of(doc.getQuantity()),
                Money.of(doc.getFilledQuantity()),
                OrderStatus.valueOf(doc.getStatus()),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.getCorrelationId(),
                doc.getVersion()
        );
    }
}
