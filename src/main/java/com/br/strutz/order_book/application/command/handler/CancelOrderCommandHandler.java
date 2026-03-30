package com.br.strutz.order_book.application.command.handler;

import com.br.strutz.order_book.application.command.CancelOrderCommand;
import com.br.strutz.order_book.application.command.CancelOrderResult;
import com.br.strutz.order_book.domain.event.OrderCancelledEvent;
import com.br.strutz.order_book.domain.exception.InvalidOrderStateException;
import com.br.strutz.order_book.domain.exception.UnauthorizedOrderCancellationException;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.port.input.CancelOrderUseCase;
import com.br.strutz.order_book.domain.port.output.event.EventPublisher;
import com.br.strutz.order_book.domain.port.output.order.OrderBookCache;
import com.br.strutz.order_book.domain.port.output.order.OrderRepository;
import com.br.strutz.order_book.domain.service.OrderBookService;
import com.br.strutz.order_book.domain.service.WalletService;
import com.br.strutz.order_book.domain.exception.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CancelOrderCommandHandler implements CancelOrderUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(CancelOrderCommandHandler.class);

    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final OrderBookService orderBook;
    private final EventPublisher eventPublisher;
    private final OrderBookCache orderBookCache;

    public CancelOrderCommandHandler(OrderRepository orderRepository,
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
    public CancelOrderResult cancel(CancelOrderCommand command) {
        log.info("Cancelling order — orderId={} userId={}",
                command.getOrderId(), command.getUserId());

        Order order = orderRepository.findById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));

        if (!order.getUserId().equals(command.getUserId())) {
            throw new UnauthorizedOrderCancellationException(
                    command.getOrderId(), command.getUserId());
        }

        if (!order.isActive()) {
            throw new InvalidOrderStateException(
                    "Ordem %s não pode ser cancelada — status: %s"
                            .formatted(order.getId(), order.getStatus()));
        }

        orderBook.remove(order);

        order.cancel();
        orderRepository.save(order);

        walletService.releaseReserve(
                order.getUserId(),
                order.getType(),
                order.getPrice(),
                order.remainingQuantity());

        
        orderBookCache.invalidate();
        eventPublisher.publish(new OrderCancelledEvent(order));

        log.info("Order cancelled — orderId={}", order.getId());

        return CancelOrderResult.success(order.getId(), order.getUserId());
    }
}