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
        List<WalletDocument.WalletTransactionEmbedded> txDocs = wallet.getTransactions().stream()
                .map(tx -> WalletDocument.WalletTransactionEmbedded.builder()
                        .type(tx.getTransactionType().name())
                        .amount(tx.getAmount().getAmount())
                        .description(tx.getDescription())
                        .occurredAt(tx.getOcurredAt())
                        .build())
                .toList();

        return WalletDocument.builder()
                .id(wallet.getId().getValue())
                .version(wallet.getVersion())
                .userId(wallet.getUserId().getValue())
                .availableBalance(wallet.getAvailableBalance().getAmount())
                .reservedBalance(wallet.getReservedBalance().getAmount())
                .transactions(txDocs)
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    public Wallet toDomain(WalletDocument doc) {
        List<WalletTransaction> transactions = doc.getTransactions().stream()
                .map(tx -> WalletTransaction.reconstitute(
                        WalletTransactionType.valueOf(tx.getType()),
                        Money.of(tx.getAmount()),
                        tx.getDescription(),
                        tx.getOccurredAt()))
                .toList();

        return Wallet.reconstitute(
                WalletId.of(doc.getId()),
                UserId.of(doc.getUserId()),
                Money.of(doc.getAvailableBalance()),
                Money.of(doc.getReservedBalance()),
                doc.getCreatedAt(),
                transactions,
                doc.getVersion()
        );
    }
}
