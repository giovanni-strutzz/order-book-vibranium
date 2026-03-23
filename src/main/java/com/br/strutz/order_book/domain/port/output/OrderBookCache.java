package com.br.strutz.order_book.domain.port.output;

import com.br.strutz.order_book.application.query.OrderBookSnapshot;

import java.util.Optional;

public interface OrderBookCache {

    void saveSnapshot(OrderBookSnapshot snapshot);
    Optional<OrderBookSnapshot> getSnapshot();
    void invalidate();
    long getTtlSeconds();
}
