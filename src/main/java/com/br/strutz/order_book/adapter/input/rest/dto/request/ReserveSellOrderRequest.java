package com.br.strutz.order_book.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReserveSellOrderRequest {

    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantity;

    public ReserveSellOrderRequest() {}

    public ReserveSellOrderRequest(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
