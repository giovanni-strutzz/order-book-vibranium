package com.br.strutz.order_book.domain.model.order;

import lombok.Value;
import org.springframework.util.Assert;

import java.util.UUID;

@Value
public class OrderId {

    String value;

    private OrderId(String value) {
        Assert.hasText(value, "OrderId cannot be null");
        this.value = value;
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
