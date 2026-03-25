package com.br.strutz.order_book.domain.port.output.order;

import com.br.strutz.order_book.adapter.output.mongo.document.OrderDocument;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.apache.catalina.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    Optional<Order> findByCorrelationId(String correlationId);

    List<Order> findActiveByType(OrderType type);

    List<Order> findActiveByUserId(UserId userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(UserId userId, int page, int size);

    boolean existsByCorrelationId(String correlationId);

    long countByUserId(UserId userId);
}
