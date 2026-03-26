package com.br.strutz.order_book.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReserveBuyOrderRequest {

    @NotNull(message = "Preço da ordem é obrigatório")
    private BigDecimal price;

    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantity;

    public ReserveBuyOrderRequest() {}

    public ReserveBuyOrderRequest(BigDecimal price, BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
