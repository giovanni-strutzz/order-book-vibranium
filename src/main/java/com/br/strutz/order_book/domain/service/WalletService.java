package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.domain.exception.WalletNotFoundException;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.output.wallet.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository repository;

    public WalletService(WalletRepository repository) {
        this.repository = repository;
    }

    public void reserveForBuyOrder(UserId userId, Money price, Money quantity) {
        Wallet wallet = findWithLock(userId);
        wallet.reserveForBuyOrder(price, quantity);
        repository.save(wallet);
    }

    public void reserveForSellOrder(UserId userId, Money quantity) {
        Wallet wallet = findWithLock(userId);
        wallet.reserveForSellOrder(quantity);
        repository.save(wallet);
    }

    public void settleTrades(List<Trade> trades) {
        trades.forEach(this::settleOneTrade);
    }

    private void settleOneTrade(Trade trade) {
        settleForBuyer(trade);
        settleForSeller(trade);
    }

    private void settleForBuyer(Trade trade) {
        Wallet buyerWallet = findWithLock(trade.getBuyerId());

        buyerWallet.debitReserved(
                trade.getTotalValue(),
                "Acquisition of %.8f VBR for %.2f — trade %s"
                        .formatted(
                                trade.getQuantity().getAmount(),
                                trade.getPrice().getAmount(),
                                trade.getId()));

        buyerWallet.creditFromTrade(
                trade.getQuantity(),
                "Collect of %.8f VBR — trade %s"
                        .formatted(
                                trade.getQuantity().getAmount(),
                                trade.getId()));

        repository.save(buyerWallet);
    }

    private void settleForSeller(Trade trade) {
        Wallet sellerWallet = findWithLock(trade.getSellerId());

        // Credita o valor em reais recebido pela venda
        sellerWallet.creditFromTrade(
                trade.getTotalValue(),
                "Venda de %.8f VBR a %.2f — trade %s"
                        .formatted(
                                trade.getQuantity().getAmount(),
                                trade.getPrice().getAmount(),
                                trade.getId()));

        // Debita o Vibranium que estava reservado
        sellerWallet.debitReserved(
                trade.getQuantity(),
                "Entrega de %.8f VBR — trade %s"
                        .formatted(
                                trade.getQuantity().getAmount(),
                                trade.getId()));

        repository.save(sellerWallet);
    }

    public void releaseReserve(UserId userId, OrderType orderType,
                               Money price, Money remainingQuantity) {
        Wallet wallet = findWithLock(userId);

        if (orderType == OrderType.BUY) {
            Money reservedAmount = price.multiply(remainingQuantity);
            wallet.releaseReserve(reservedAmount);
        } else {
            wallet.releaseReserve(remainingQuantity);
        }

        repository.save(wallet);
    }

    public Wallet findWallet(UserId userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    private Wallet findWithLock(UserId userId) {
        return repository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }
}
