package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.port.output.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderBookInitializerService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderBookInitializerService.class);

    private final OrderBookService orderBook;
    private final OrderRepository orderRepository;

    public OrderBookInitializerService(OrderBookService orderBook,
                                OrderRepository orderRepository) {
        this.orderBook       = orderBook;
        this.orderRepository = orderRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        log.info("Initializing the Order Book from MongoDB...");

        List<Order> pendingBuys  = orderRepository.findActiveByType(OrderType.BUY);
        List<Order> pendingSells = orderRepository.findActiveByType(OrderType.SELL);

        orderBook.initialize(pendingBuys, pendingSells);

        log.info("Initialize OrderBook — {} bids, {} asks",
                orderBook.getBidDepth(),
                orderBook.getAskDepth());
    }
}
