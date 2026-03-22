package com.br.strutz.order_book.domain.model;

import com.br.strutz.order_book.domain.exception.InsufficientFundsException;
import com.br.strutz.order_book.domain.exception.InvalidMoneyException;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Value
public class Money {

    BigDecimal amount;

    static final String CURRENCY = "BRL";
    static final int SCALE = 8;

    private Money(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyException("Money cannot be negative: " + amount);
        }

        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money anotherCurrency) {
        return new Money(this.amount.add(anotherCurrency.amount));
    }

    public Money substract(Money anotherCurrency) {

        if (this.amount.compareTo(anotherCurrency.amount) <= 0) {
            throw new InsufficientFundsException(
                    "Insuficient Funds: Current Funds %s".formatted(this.amount)
            );
        }

        return new Money(this.amount.subtract(anotherCurrency.amount));
    }

    public Money multiply(Money quantity) {
        return new Money(this.amount.multiply(quantity.amount)
                .setScale(SCALE, RoundingMode.HALF_UP));
    }

    public boolean gte(Money anotherAmount) {
        return this.amount.compareTo(anotherAmount.amount) >= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
