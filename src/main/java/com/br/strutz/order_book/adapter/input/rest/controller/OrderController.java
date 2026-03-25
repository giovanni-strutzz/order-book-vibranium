package com.br.strutz.order_book.adapter.input.rest.controller;

import com.br.strutz.order_book.adapter.input.rest.dto.request.CancelOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.request.PlaceOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.response.OrderBookResponse;
import com.br.strutz.order_book.adapter.input.rest.dto.response.OrderResponse;
import com.br.strutz.order_book.adapter.input.rest.mapper.OrderRestMapper;
import com.br.strutz.order_book.adapter.input.rest.swagger.OrderSwagger;
import com.br.strutz.order_book.adapter.output.mongo.mapper.OrderMapper;
import com.br.strutz.order_book.application.command.CancelOrderCommand;
import com.br.strutz.order_book.application.command.CancelOrderResult;
import com.br.strutz.order_book.application.command.PlaceOrderCommand;
import com.br.strutz.order_book.application.command.PlaceOrderResult;
import com.br.strutz.order_book.application.query.GetOrderBook;
import com.br.strutz.order_book.application.query.OrderBookSnapshot;
import com.br.strutz.order_book.domain.port.input.CancelOrderUseCase;
import com.br.strutz.order_book.domain.port.input.GetOrderBookUseCase;
import com.br.strutz.order_book.domain.port.input.PlaceOrderUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrderSwagger {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderBookUseCase getOrderBookUseCase;
    private final OrderRestMapper mapper;

    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase,
                           GetOrderBookUseCase getOrderBookUseCase,
                           OrderRestMapper mapper) {
        this.placeOrderUseCase  = placeOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getOrderBookUseCase = getOrderBookUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<OrderResponse> placeOrder(PlaceOrderRequest request,
                                                    String correlationId) {
        PlaceOrderCommand command = mapper.toPlaceCommand(request, correlationId);
        PlaceOrderResult result   = placeOrderUseCase.place(command);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(mapper.toResponse(result));
    }

    @Override
    public ResponseEntity<OrderResponse> cancelOrder(String orderId,
                                                     CancelOrderRequest request,
                                                     String correlationId) {
        CancelOrderCommand command = mapper.toCancelCommand(orderId, request, correlationId);
        CancelOrderResult result   = cancelOrderUseCase.cancel(command);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @Override
    public ResponseEntity<OrderBookResponse> getOrderBook(int depth) {
        OrderBookSnapshot snapshot = getOrderBookUseCase
                .handle(GetOrderBook.withDefaultDepth());
        return ResponseEntity.ok(mapper.toResponse(snapshot));
    }

    @Override
    public ResponseEntity<?> getOrderHistory(String userId, int page, int size) {
        return ResponseEntity.ok().build();
    }
}