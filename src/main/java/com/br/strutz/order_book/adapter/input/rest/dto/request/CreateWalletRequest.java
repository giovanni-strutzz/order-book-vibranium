package com.br.strutz.order_book.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateWalletRequest {

    @NotBlank(message = "UserId é obrigatório")
    private String userId;

    @NotNull(message = "Saldo inicial é obrigatório")
    private BigDecimal initialBalance;

    public CreateWalletRequest() {
    }

    public CreateWalletRequest(String userId, BigDecimal initialBalance) {
        this.userId = userId;
        this.initialBalance = initialBalance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
