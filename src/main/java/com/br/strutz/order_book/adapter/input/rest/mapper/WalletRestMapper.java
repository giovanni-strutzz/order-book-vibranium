package com.br.strutz.order_book.adapter.input.rest.mapper;

import com.br.strutz.order_book.adapter.input.rest.dto.response.TransactionHistoryResponse;
import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletResponse;
import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletTransactionResponse;
import com.br.strutz.order_book.application.query.WalletSnapshot;
import com.br.strutz.order_book.domain.model.aggregates.WalletTransaction;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletRestMapper {

    public WalletResponse toResponse(WalletSnapshot snapshot) {
        return WalletResponse.builder()
                .userId(snapshot.getUserId())
                .availableBalance(snapshot.getAvailableBalance())
                .reservedBalance(snapshot.getReservedBalance())
                .totalBalance(snapshot.getTotalBalance())
                .updatedAt(snapshot.getUpdatedAt())
                .build();
    }

    public TransactionHistoryResponse toTransactionHistoryResponse(
            UserId userId,
            List<WalletTransaction> transactions,
            int page,
            int size,
            long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new TransactionHistoryResponse(
                userId,
                toTransactionResponseList(transactions),
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public List<WalletTransactionResponse> toTransactionResponseList(
            List<WalletTransaction> transactions) {
        return transactions.stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private WalletTransactionResponse toTransactionResponse(WalletTransaction tx) {
        return new WalletTransactionResponse(
                tx.getTransactionType().name(),
                tx.getAmount().getAmount(),
                tx.getDescription(),
                tx.getOcurredAt()
        );
    }
}
