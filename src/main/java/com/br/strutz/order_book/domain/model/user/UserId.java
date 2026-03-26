package com.br.strutz.order_book.domain.model.user;

import lombok.Value;
import org.springframework.util.Assert;

@Value
public class UserId {

    String value;

    private UserId(String value) {
        Assert.hasText(value, "UserId cannot be null");
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
