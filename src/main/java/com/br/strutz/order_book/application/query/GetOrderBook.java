package com.br.strutz.order_book.application.query;

import lombok.Value;

@Value
public class GetOrderBook {

    int depth;

    public static GetOrderBook withDefautlDepth() {
        return new GetOrderBook(20);
    }
}
