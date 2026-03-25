package com.br.strutz.order_book.application.command.handler;

import com.br.strutz.order_book.application.command.PlaceOrderCommand;
import com.br.strutz.order_book.application.command.PlaceOrderResult;
import com.br.strutz.order_book.domain.event.BalanceUpdatedEvent;
import com.br.strutz.order_book.domain.event.OrderMatchedEvent;
import com.br.strutz.order_book.domain.event.OrderPlacedEvent;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.port.input.PlaceOrderUseCase;
import com.br.strutz.order_book.domain.port.output.event.EventPublisher;
import com.br.strutz.order_book.domain.port.output.order.OrderBookCache;
import com.br.strutz.order_book.domain.port.output.order.OrderRepository;
import com.br.strutz.order_book.domain.service.OrderBookService;
import com.br.strutz.order_book.domain.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceOrderCommandHandler implements PlaceOrderUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(PlaceOrderCommandHandler.class);

    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final OrderBookService orderBook;
    private final EventPublisher eventPublisher;
    private final OrderBookCache orderBookCache;

    public PlaceOrderCommandHandler(OrderRepository orderRepository,
                                    WalletService walletService,
                                    OrderBookService orderBook,
                                    EventPublisher eventPublisher,
                                    OrderBookCache orderBookCache) {
        this.orderRepository = orderRepository;
        this.walletService   = walletService;
        this.orderBook       = orderBook;
        this.eventPublisher  = eventPublisher;
        this.orderBookCache  = orderBookCache;
    }

    @Override
    public PlaceOrderResult place(PlaceOrderCommand command) {
        log.info("Placing order — user={} type={} price={} qty={} correlationId={}",
                command.getUserId(), command.getOrderType(),
                command.getPrice(), command.getQuantity(),
                command.getCorrelationId());

        if (orderRepository.existsByCorrelationId(command.getCorrelationId())) {
            log.warn("Duplicate order rejected — correlationId={}",
                    command.getCorrelationId());
            return PlaceOrderResult.rejected(
                    "Ordem já processada para correlationId: "
                            + command.getCorrelationId());
        }

        reserveBalance(command);

        Order order = Order.create(
                command.getUserId(),
                command.getOrderType(),
                command.getPrice(),
                command.getQuantity(),
                command.getCorrelationId()
        );

        order = orderRepository.save(order);

        List<Trade> trades = orderBook.match(order);

//        orderRepository(order);

        if (!trades.isEmpty()) {
            walletService.settleTrades(trades);
            trades.forEach(trade -> {
                eventPublisher.publish(new OrderMatchedEvent(trade));
                eventPublisher.publish(new BalanceUpdatedEvent(trade));
            });
        }

        // 7. Invalida cache do book — será reconstruído na próxima leitura
        orderBookCache.invalidate();

        // 8. Publica evento de ordem colocada
        eventPublisher.publish(new OrderPlacedEvent(order));

        log.info("Order placed — orderId={} trades={}",
                order.getId(), trades.size());

        return PlaceOrderResult.success(order.getId());
    }

    private void reserveBalance(PlaceOrderCommand command) {
        if (command.getOrderType() == OrderType.BUY) {
            walletService.reserveForBuyOrder(
                    command.getUserId(),
                    command.getPrice(),
                    command.getQuantity());
        } else {
            walletService.reserveForSellOrder(
                    command.getUserId(),
                    command.getQuantity());
        }
    }
}
