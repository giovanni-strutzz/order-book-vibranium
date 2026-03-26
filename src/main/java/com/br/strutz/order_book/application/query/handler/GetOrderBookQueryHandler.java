package com.br.strutz.order_book.application.query.handler;

import com.br.strutz.order_book.application.query.GetOrderBook;
import com.br.strutz.order_book.application.query.OrderBookSnapshot;
import com.br.strutz.order_book.domain.port.input.GetOrderBookUseCase;
import com.br.strutz.order_book.domain.port.output.order.OrderBookCache;
import com.br.strutz.order_book.domain.service.OrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetOrderBookQueryHandler implements GetOrderBookUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(GetOrderBookQueryHandler.class);

    private final OrderBookCache orderBookCache;
    private final OrderBookService orderBook;

    public GetOrderBookQueryHandler(OrderBookCache orderBookCache,
                                    OrderBookService orderBook) {
        this.orderBookCache = orderBookCache;
        this.orderBook      = orderBook;
    }

    @Override
    public OrderBookSnapshot handle(GetOrderBook query) {
        return orderBookCache.getSnapshot().orElseGet(() -> {
            log.debug("Cache miss — rebuilding snapshot depth={}",
                    query.getDepth());

            OrderBookSnapshot snapshot = orderBook.toSnapshot(query.getDepth());

            orderBookCache.saveSnapshot(snapshot);

            return snapshot;
        });
    }
}