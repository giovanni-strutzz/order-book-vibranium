package com.br.strutz.order_book.domain.model.aggregates;

import com.br.strutz.order_book.domain.exception.InsufficientFundsException;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.model.wallet.WalletId;
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
@EqualsAndHashCode(of = "id")
@ToString
public class Wallet {

    private final WalletId id;
    private final UserId   userId;
    private final Instant  createdAt;
    private       Money    availableBalance;
    private       Money    reservedBalance;
    private       Instant  updatedAt;

    private final List<WalletTransaction> transactions = new ArrayList<>();

    private Wallet(WalletId id, UserId userId, Money initialBalance) {
        this.id               = Objects.requireNonNull(id);
        this.userId           = Objects.requireNonNull(userId);
        this.availableBalance = Objects.requireNonNull(initialBalance);
        this.reservedBalance  = Money.zero();
        this.createdAt        = Instant.now();
        this.updatedAt        = this.createdAt;
    }

    // Factory method — gera um novo WalletId independente do UserId
    public static Wallet create(UserId userId, Money initialBalance) {
        return new Wallet(WalletId.generate(), userId, initialBalance);
    }

    // Reconstitui a partir da persistência — preserva o id original
    public static Wallet reconstitute(WalletId id,
                                      UserId userId,
                                      Money available,
                                      Money reserved,
                                      Instant createdAt,
                                      List<WalletTransaction> transactions) {
        Wallet wallet          = new Wallet(id, userId, available);
        wallet.reservedBalance = reserved;
        wallet.updatedAt       = Instant.now();
        wallet.transactions.addAll(transactions);
        return wallet;
    }

    public void reserveForBuyOrder(Money price, Money quantity) {
        Money totalCost = price.multiply(quantity);
        if (!availableBalance.gte(totalCost)) {
            throw new InsufficientFundsException(
                    "Insufficient funds to reserve %s. Available: %s"
                            .formatted(totalCost, availableBalance));
        }
        availableBalance = availableBalance.substract(totalCost);
        reservedBalance  = reservedBalance.add(totalCost);
        recordTransaction(WalletTransactionType.RESERVE, totalCost,
                "Reserve for buy order");
        this.updatedAt = Instant.now();
    }

    public void reserveForSellOrder(Money quantity) {
        if (!availableBalance.gte(quantity)) {
            throw new InsufficientFundsException(
                    "Insufficient Vibranium to reserve %s. Available: %s"
                            .formatted(quantity, availableBalance));
        }
        availableBalance = availableBalance.substract(quantity);
        reservedBalance  = reservedBalance.add(quantity);
        recordTransaction(WalletTransactionType.RESERVE, quantity,
                "Reserve for sell order");
        this.updatedAt = Instant.now();
    }

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
                "Reserve release — order cancelled");
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
