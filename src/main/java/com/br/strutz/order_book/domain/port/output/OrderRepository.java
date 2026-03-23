package com.br.strutz.order_book.domain.port.output;

import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
    List<Order> findActiveByType(OrderType orderType);
    List<Order> findActiveByUserId(UserId userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(UserId userId, int page, int size);
    boolean existsByCorrelationId(String correlationId);
}
