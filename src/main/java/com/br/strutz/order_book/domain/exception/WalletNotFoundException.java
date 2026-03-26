package com.br.strutz.order_book.domain.exception;

import com.br.strutz.order_book.domain.model.user.UserId;

public class WalletNotFoundException extends DomainException {

    public WalletNotFoundException(UserId userId) {
        super("Not Found Wallet for User: " + userId.getValue());
    }
}
