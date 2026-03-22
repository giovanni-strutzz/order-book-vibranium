package com.br.strutz.order_book.domain.model;

import lombok.Value;
import org.springframework.util.Assert;

import java.util.UUID;

@Value
public class TradeId {

    String value;

    private TradeId(String value) {
        Assert.hasText(value, "TradeId cannot be blank");
        this.value = value;
    }

    public static TradeId generate() {
        return new TradeId(UUID.randomUUID().toString());
    }

    public static TradeId of(String value) {
        return new TradeId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
