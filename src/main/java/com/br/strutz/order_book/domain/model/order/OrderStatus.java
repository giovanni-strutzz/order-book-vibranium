package com.br.strutz.order_book.domain.model.order;

public enum OrderStatus {

    PENDING,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED;

    public boolean isActive() {
        return this == PENDING || this == PARTIALLY_FILLED;
    }

    public boolean isTerminal() {
        return this == FILLED || this == CANCELLED;
    }
}
