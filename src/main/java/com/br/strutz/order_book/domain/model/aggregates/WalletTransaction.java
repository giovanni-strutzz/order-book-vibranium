package com.br.strutz.order_book.domain.model.aggregates;

import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.wallet.WalletTransactionType;
import lombok.Value;

import java.time.Instant;

@Value
public class WalletTransaction {

    WalletTransactionType transactionType;
    Money amount;
    String description;
    Instant ocurredAt;

    private WalletTransaction(WalletTransactionType transactionType, Money amount, String description) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.ocurredAt = Instant.now();
    };

    public static WalletTransaction of(WalletTransactionType transactionType, Money amount, String description) {
        return new WalletTransaction(transactionType, amount, description);
    }
}
