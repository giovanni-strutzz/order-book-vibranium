package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.query.GetTradeHistoryQuery;
import com.br.strutz.order_book.application.query.TradeHistoryResult;

public interface GetTradeHistoryUseCase {

    TradeHistoryResult handle(GetTradeHistoryQuery query);
}
