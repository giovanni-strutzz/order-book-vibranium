package com.br.strutz.order_book.application.query;

import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetTradeHistoryQuery {

    UserId userId;
    int page;
    int size;

    public static GetTradeHistoryQuery of(UserId userId, int page, int size) {
        return new GetTradeHistoryQuery(userId, page, size);
    }
}
