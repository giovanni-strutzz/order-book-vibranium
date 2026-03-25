package com.br.strutz.order_book.application.command;

import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.user.UserId;

public class CreateWalletCommand {

    private final UserId userId;
    private final Money initialBalance;

    public CreateWalletCommand(UserId userId, Money initialBalance) {
        this.userId = userId;
        this.initialBalance = initialBalance;
    }

    public UserId getUserId() {
        return userId;
    }

    public Money getInitialBalance() {
        return initialBalance;
    }
}
