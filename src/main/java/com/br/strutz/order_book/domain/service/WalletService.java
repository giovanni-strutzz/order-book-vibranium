package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.domain.exception.WalletAlreadyExistsException;
import com.br.strutz.order_book.domain.exception.WalletNotFoundException;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.output.wallet.WalletRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class WalletService {

    private static final long LOCK_WAIT_SECONDS    = 3;
    private static final long LOCK_LEASE_SECONDS   = 5;

    private final WalletRepository repository;
    private final RedissonClient redissonClient;

    public WalletService(WalletRepository repository, RedissonClient redissonClient) {
        this.repository     = repository;
        this.redissonClient = redissonClient;
    }

    public Wallet createWallet(UserId userId, Money initialBalance) {
        if (repository.existsByUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }

        Wallet wallet = Wallet.create(userId, initialBalance);

        return repository.save(wallet);
    }


    public void reserveForBuyOrder(UserId userId, Money price, Money quantity) {
        executeWithLock(userId, wallet -> {
            // Se não existe, lance exceção
            if (wallet == null) {
                throw new WalletNotFoundException(userId);
            }
            wallet.reserveForBuyOrder(price, quantity);
            repository.save(wallet); // update
        });
    }

    public void reserveForSellOrder(UserId userId, Money quantity) {
        executeWithLock(userId, wallet -> {
            if (wallet == null) {
                throw new WalletNotFoundException(userId);
            }
            wallet.reserveForSellOrder(quantity);
            repository.save(wallet);
        });
    }

    public void settleTrades(List<Trade> trades) {
        trades.forEach(this::settleOneTrade);
    }

    public void releaseReserve(UserId userId, OrderType orderType,
                               Money price, Money remainingQuantity) {
        executeWithLock(userId, wallet -> {
            if (orderType == OrderType.BUY) {
                wallet.releaseReserve(price.multiply(remainingQuantity));
            } else {
                wallet.releaseReserve(remainingQuantity);
            }
            repository.save(wallet);
        });
    }

    public Wallet findWallet(UserId userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private void settleOneTrade(Trade trade) {
        settleForBuyer(trade);
        settleForSeller(trade);
    }

    private void settleForBuyer(Trade trade) {
        executeWithLock(trade.getBuyerId(), wallet -> {
            wallet.debitReserved(
                    trade.getTotalValue(),
                    "Acquisition of %.8f VBR for %.2f — trade %s"
                            .formatted(
                                    trade.getQuantity().getAmount(),
                                    trade.getPrice().getAmount(),
                                    trade.getId()));

            wallet.creditFromTrade(
                    trade.getQuantity(),
                    "Collect of %.8f VBR — trade %s"
                            .formatted(
                                    trade.getQuantity().getAmount(),
                                    trade.getId()));

            repository.save(wallet);
        });
    }

    private void settleForSeller(Trade trade) {
        executeWithLock(trade.getSellerId(), wallet -> {
            wallet.creditFromTrade(
                    trade.getTotalValue(),
                    "Venda de %.8f VBR a %.2f — trade %s"
                            .formatted(
                                    trade.getQuantity().getAmount(),
                                    trade.getPrice().getAmount(),
                                    trade.getId()));

            wallet.debitReserved(
                    trade.getQuantity(),
                    "Entrega de %.8f VBR — trade %s"
                            .formatted(
                                    trade.getQuantity().getAmount(),
                                    trade.getId()));

            repository.save(wallet);
        });
    }

    private void executeWithLock(UserId userId, Consumer<Wallet> action) {
        RLock lock = redissonClient.getLock("wallet-lock:" + userId.getValue());
        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException(
                        "Could not acquire lock for wallet: " + userId.getValue());
            }
            try {
                Wallet wallet = repository.findByUserId(userId)
                        .orElseThrow(() -> new WalletNotFoundException(userId));
                action.accept(wallet);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Lock interrupted for wallet: " + userId.getValue());
        }
    }
}
