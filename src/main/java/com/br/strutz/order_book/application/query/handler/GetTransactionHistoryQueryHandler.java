package com.br.strutz.order_book.application.query.handler;

import com.br.strutz.order_book.adapter.input.rest.dto.response.TransactionHistoryResponse;
import com.br.strutz.order_book.adapter.input.rest.mapper.WalletRestMapper;
import com.br.strutz.order_book.application.query.GetTransactionHistoryQuery;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.aggregates.WalletTransaction;
import com.br.strutz.order_book.domain.port.input.GetTransactionHistoryUseCase;
import com.br.strutz.order_book.domain.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTransactionHistoryQueryHandler implements GetTransactionHistoryUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(GetTransactionHistoryQueryHandler.class);

    private final WalletService    walletService;
    private final WalletRestMapper walletRestMapper;

    public GetTransactionHistoryQueryHandler(WalletService walletService,
                                             WalletRestMapper walletRestMapper) {
        this.walletService    = walletService;
        this.walletRestMapper = walletRestMapper;
    }

    @Override
    public TransactionHistoryResponse handle(GetTransactionHistoryQuery query) {
        log.info("Fetching Transaction History");

        Wallet wallet = walletService.findWallet(query.getUserId());

        List<WalletTransaction> allTransactions = wallet.getTransactions();

        int totalElements = allTransactions.size();
        int fromIndex     = query.getPage() * query.getSize();
        int toIndex       = Math.min(fromIndex + query.getSize(), totalElements);

        List<WalletTransaction> paged = fromIndex >= totalElements
                ? List.of()
                : allTransactions.subList(fromIndex, toIndex);

        log.info("Transaction History Fetched");

        return walletRestMapper.toTransactionHistoryResponse(
                query.getUserId(),
                paged,
                query.getPage(),
                query.getSize(),
                totalElements
        );
    }
}
