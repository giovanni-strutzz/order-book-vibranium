package com.br.strutz.order_book.application.query;

import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Value;

@Value
public class GetWalletQuery {

    UserId userId;

    public static GetWalletQuery of(UserId userId) {
        return new GetWalletQuery(userId);
    }
}
