package com.br.strutz.order_book.domain.model.order;

public enum OrderType {
    BUY, SELL;

    public OrderType opposite() {
        return this == BUY ? SELL : BUY;
    }
}
