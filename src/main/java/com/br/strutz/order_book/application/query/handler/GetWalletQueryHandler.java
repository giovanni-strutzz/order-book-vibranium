package com.br.strutz.order_book.application.query.handler;

import com.br.strutz.order_book.application.query.GetWalletQuery;
import com.br.strutz.order_book.application.query.WalletSnapshot;
import com.br.strutz.order_book.domain.exception.WalletNotFoundException;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.port.input.GetWalletUseCase;
import com.br.strutz.order_book.domain.port.output.wallet.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class GetWalletQueryHandler implements GetWalletUseCase {

    private final WalletRepository walletRepository;

    public GetWalletQueryHandler(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public WalletSnapshot handle(GetWalletQuery query) {
        Wallet wallet = walletRepository.findByUserId(query.getUserId())
                .orElseThrow(() -> new WalletNotFoundException(query.getUserId()));

        return WalletSnapshot.builder()
                .userId(wallet.getUserId().getValue())
                .availableBalance(wallet.getAvailableBalance().getAmount())
                .reservedBalance(wallet.getReservedBalance().getAmount())
                .totalBalance(wallet.totalBalance().getAmount())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
