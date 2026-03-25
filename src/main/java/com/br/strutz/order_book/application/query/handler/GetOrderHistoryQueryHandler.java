package com.br.strutz.order_book.application.query.handler;

import com.br.strutz.order_book.application.query.GetOrderHistoryQuery;
import com.br.strutz.order_book.application.query.OrderSnapshot;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.port.input.GetOrderHistoryUseCase;
import com.br.strutz.order_book.domain.port.output.order.OrderRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetOrderHistoryQueryHandler implements GetOrderHistoryUseCase {

    private final OrderRepository orderRepository;

    public GetOrderHistoryQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Page<OrderSnapshot> handle(GetOrderHistoryQuery query) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                query.getUserId(),
                query.getPage(),
                query.getSize()
        );

        // Converte para snapshots
        List<OrderSnapshot> snapshots = orders.stream()
                .map(order -> OrderSnapshot.builder()
                        .orderId(order.getId().toString())
                        .userId(order.getUserId().getValue())
                        .orderType(order.getType().name())
                        .price(order.getPrice().getAmount())
                        .quantity(order.getQuantity().getAmount())
                        .status(order.getStatus().name())
                        .createdAt(order.getCreatedAt())
                        .updatedAt(order.getUpdatedAt())
                        .correlationId(order.getCorrelationId())
                        .build()
                )
                .toList();

        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(), Sort.by("createdAt").descending());

        long total = orderRepository.countByUserId(query.getUserId());

        return new PageImpl<>(snapshots, pageable, total);
    }
}
