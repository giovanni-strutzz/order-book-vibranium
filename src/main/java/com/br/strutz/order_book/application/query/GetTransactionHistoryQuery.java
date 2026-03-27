package com.br.strutz.order_book.application.query;

import com.br.strutz.order_book.domain.model.user.UserId;

public class GetTransactionHistoryQuery {

    private final UserId userId;
    private final int page;
    private final int size;

    private GetTransactionHistoryQuery(UserId userId, int page, int size) {
        this.userId = userId;
        this.page   = page;
        this.size   = size;
    }

    public static GetTransactionHistoryQuery of(UserId userId, int page, int size) {
        return new GetTransactionHistoryQuery(userId, page, size);
    }

    public UserId getUserId() { return userId; }
    public int    getPage()   { return page; }
    public int    getSize()   { return size; }
}
