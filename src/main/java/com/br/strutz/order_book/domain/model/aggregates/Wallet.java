package com.br.strutz.order_book.domain.model.aggregates;

import com.br.strutz.order_book.domain.exception.InsufficientFundsException;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.model.wallet.WalletTransactionType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "userId")
@ToString
public class Wallet {

    private final UserId userId;
    private final Instant createdAt;
    private Money availableBalance;
    private Money reservedBalance;
    private Instant updatedAt;

    // Histórico imutável de movimentações (Event Sourcing leve)
    private final List<WalletTransaction> transactions = new ArrayList<>();

    private Wallet(UserId userId, Money initialBalance) {
        this.userId           = Objects.requireNonNull(userId);
        this.availableBalance = Objects.requireNonNull(initialBalance);
        this.reservedBalance  = Money.zero();
        this.createdAt        = Instant.now();
        this.updatedAt        = this.createdAt;
    }

    public static Wallet create(UserId userId, Money initialBalance) {
        return new Wallet(userId, initialBalance);
    }

    public static Wallet reconstitute(UserId userId, Money available,
                                      Money reserved, Instant createdAt,
                                      List<WalletTransaction> transactions) {
        var wallet = new Wallet(userId, available);
        wallet.reservedBalance = reserved;
        wallet.updatedAt       = Instant.now();
        wallet.transactions.addAll(transactions);
        return wallet;
    };

    /**
     * Reserve funds for a Buy Order (Before Match)
     * @param price - Price of Order (BRL)
     * @param quantity - Quantity of Orders
     */
    public void reserveForBuyOrder(Money price, Money quantity) {

        var totalCost = price.multiply(quantity);

        if (!availableBalance.gte(totalCost)) {
            throw new InsufficientFundsException(
                    "Insufficient funds to reserve %s. Available: %s".formatted(totalCost, availableBalance)
            );
        }

        availableBalance = availableBalance.substract(totalCost);
        reservedBalance = reservedBalance.add(totalCost);

        recordTransaction(WalletTransactionType.RESERVE, totalCost, "Buy Order reserve");

        this.updatedAt = Instant.now();
    }

    public void reserveForSellOrder(Money quantity) {

        if(!availableBalance.gte(quantity)) {
            throw new InsufficientFundsException(
                    "Insufficent Vibranium to reserve %s. Available: %s".formatted(quantity, availableBalance)
            );
        }

        availableBalance = availableBalance.substract(quantity);
        reservedBalance  = reservedBalance.add(quantity);

        recordTransaction(WalletTransactionType.RESERVE, quantity,
                "Buy Order reserve");

        this.updatedAt = Instant.now();
    };

    public void creditFromTrade(Money amount, String description) {
        availableBalance = availableBalance.add(amount);
        recordTransaction(WalletTransactionType.CREDIT, amount, description);
        this.updatedAt = Instant.now();
    }

    public void debitReserved(Money amount, String description) {
        reservedBalance = reservedBalance.substract(amount);
        recordTransaction(WalletTransactionType.DEBIT, amount, description);
        this.updatedAt = Instant.now();
    }

    public void releaseReserve(Money amount) {
        reservedBalance  = reservedBalance.substract(amount);
        availableBalance = availableBalance.add(amount);
        recordTransaction(WalletTransactionType.RELEASE, amount,
                "Liberação de reserva — ordem cancelada");
        this.updatedAt = Instant.now();
    }

    public Money totalBalance() {
        return availableBalance.add(reservedBalance);
    }

    public List<WalletTransaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    private void recordTransaction(WalletTransactionType type,
                                   Money amount, String description) {
        transactions.add(WalletTransaction.of(type, amount, description));
    }
}
