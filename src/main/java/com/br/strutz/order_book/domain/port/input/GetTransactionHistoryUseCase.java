package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.adapter.input.rest.dto.response.TransactionHistoryResponse;
import com.br.strutz.order_book.application.query.GetTransactionHistoryQuery;

public interface GetTransactionHistoryUseCase {

    TransactionHistoryResponse handle(GetTransactionHistoryQuery query);
}
