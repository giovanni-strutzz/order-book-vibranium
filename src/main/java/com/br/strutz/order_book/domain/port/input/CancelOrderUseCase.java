package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.command.CancelOrderCommand;
import com.br.strutz.order_book.application.command.CancelOrderResult;

public interface CancelOrderUseCase {

    CancelOrderResult cancel(CancelOrderCommand command);
}
