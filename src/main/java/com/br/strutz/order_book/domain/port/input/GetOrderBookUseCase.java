package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.query.GetOrderBook;
import com.br.strutz.order_book.application.query.OrderBookSnapshot;

public interface GetOrderBookUseCase {

    OrderBookSnapshot handle(GetOrderBook query);
}
