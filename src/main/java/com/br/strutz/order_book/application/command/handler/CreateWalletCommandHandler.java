package com.br.strutz.order_book.application.command.handler;

import com.br.strutz.order_book.application.command.CreateWalletCommand;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.port.input.CreateWalletUseCase;
import com.br.strutz.order_book.domain.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateWalletCommandHandler implements CreateWalletUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateWalletCommandHandler.class);
    private final WalletService walletService;

    public CreateWalletCommandHandler(WalletService walletService) {
        this.walletService = walletService;
    };

    @Override
    public Wallet handle(CreateWalletCommand command) {
        LOGGER.info("Creating wallet — userId={} initialBalance={}",
                command.getUserId(), command.getInitialBalance());

        Wallet wallet = walletService.createWallet(command.getUserId(), command.getInitialBalance());

        LOGGER.info("Wallet created — walletId={} userId={} initialBalance={}",
                wallet.getId(), command.getUserId(), wallet.getAvailableBalance());

        return wallet;
    }
}