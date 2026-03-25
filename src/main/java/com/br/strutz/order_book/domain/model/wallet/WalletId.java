package com.br.strutz.order_book.domain.model.wallet;

import org.springframework.util.Assert;

import java.util.UUID;

public class WalletId {

    String value;

    private WalletId(String value) {
        Assert.hasText(value, "WalletId cannot be null");
        this.value = value;
    }

    public static WalletId generate() {
        return new WalletId(UUID.randomUUID().toString());
    }

    public static WalletId of(String value) {
        return new WalletId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
