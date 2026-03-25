package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.command.CreateWalletCommand;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;

public interface CreateWalletUseCase {

    Wallet handle(CreateWalletCommand command);
}
