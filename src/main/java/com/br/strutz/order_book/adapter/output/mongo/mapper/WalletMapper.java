package com.br.strutz.order_book.adapter.output.mongo.mapper;

import com.br.strutz.order_book.adapter.output.mongo.document.WalletDocument;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.aggregates.WalletTransaction;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.model.wallet.WalletId;
import com.br.strutz.order_book.domain.model.wallet.WalletTransactionType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletMapper {

    public WalletDocument toDocument(Wallet wallet) {
        return WalletDocument.builder()
                .id(wallet.getUserId().getValue())     // _id = userId
                .userId(wallet.getUserId().getValue()) // índice único
                .availableBalance(wallet.getAvailableBalance().getAmount())
                .reservedBalance(wallet.getReservedBalance().getAmount())
                .transactions(wallet.getTransactions().stream()
                        .map(tx -> WalletDocument.WalletTransactionEmbedded.builder()
                                .type(tx.getTransactionType().name())
                                .amount(tx.getAmount().getAmount())
                                .description(tx.getDescription())
                                .occurredAt(tx.getOcurredAt())
                                .build())
                        .toList())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    public Wallet toDomain(WalletDocument doc) {
        List<WalletTransaction> transactions = doc.getTransactions().stream()
                .map(tx -> WalletTransaction.of(
                        WalletTransactionType.valueOf(tx.getType()),
                        Money.of(tx.getAmount()),
                        tx.getDescription()))
                .toList();

        return Wallet.reconstitute(
                WalletId.of(doc.getId()),
                UserId.of(doc.getUserId()),
                Money.of(doc.getAvailableBalance()),
                Money.of(doc.getReservedBalance()),
                doc.getCreatedAt(),
                transactions
        );
    }
}
