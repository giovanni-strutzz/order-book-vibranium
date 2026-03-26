package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.command.PlaceOrderCommand;
import com.br.strutz.order_book.application.command.PlaceOrderResult;

public interface PlaceOrderUseCase {

    PlaceOrderResult place(PlaceOrderCommand command);
}
