package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.query.GetOrderHistoryQuery;
import com.br.strutz.order_book.application.query.OrderSnapshot;
import org.springframework.data.domain.Page;

public interface GetOrderHistoryUseCase {

    Page<OrderSnapshot> handle(GetOrderHistoryQuery query);
}
