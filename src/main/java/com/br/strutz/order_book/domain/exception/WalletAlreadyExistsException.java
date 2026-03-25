package com.br.strutz.order_book.domain.exception;

import com.br.strutz.order_book.domain.model.user.UserId;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(UserId userId) {
        super("Wallet already exists for User: " + userId.getValue());
    }
}
