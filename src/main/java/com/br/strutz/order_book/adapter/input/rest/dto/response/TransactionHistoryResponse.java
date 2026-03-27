package com.br.strutz.order_book.adapter.input.rest.dto.response;

import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {

    private UserId userId;
    private List<WalletTransactionResponse> transactions;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
