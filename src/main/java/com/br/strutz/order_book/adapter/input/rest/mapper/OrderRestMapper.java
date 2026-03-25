package com.br.strutz.order_book.adapter.input.rest.mapper;

import com.br.strutz.order_book.adapter.input.rest.dto.request.CancelOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.request.PlaceOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.response.OrderBookResponse;
import com.br.strutz.order_book.adapter.input.rest.dto.response.OrderResponse;
import com.br.strutz.order_book.application.command.CancelOrderCommand;
import com.br.strutz.order_book.application.command.CancelOrderResult;
import com.br.strutz.order_book.application.command.PlaceOrderCommand;
import com.br.strutz.order_book.application.command.PlaceOrderResult;
import com.br.strutz.order_book.application.query.OrderBookSnapshot;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderRestMapper {

    public PlaceOrderCommand toPlaceCommand(PlaceOrderRequest request,
                                            String correlationId) {
        return PlaceOrderCommand.builder()
                .userId(UserId.of(request.getUserId()))
                .orderType(OrderType.valueOf(request.getType()))
                .price(Money.of(request.getPrice()))
                .quantity(Money.of(request.getQuantity()))
                .correlationId(correlationId)
                .build();
    }

    public CancelOrderCommand toCancelCommand(String orderId,
                                              CancelOrderRequest request,
                                              String correlationId) {
        return CancelOrderCommand.builder()
                .orderId(OrderId.of(orderId))
                .userId(UserId.of(request.getUserId()))
                .correlationId(correlationId)
                .build();
    }

    public OrderResponse toResponse(PlaceOrderResult result) {
        return OrderResponse.builder()
                .orderId(result.getOrderId() != null
                        ? result.getOrderId().getValue() : null)
                .status(result.getStatus())
                .message(result.getMessage())
                .build();
    }

    public OrderResponse toResponse(CancelOrderResult result) {
        return OrderResponse.builder()
                .orderId(result.getOrderId() != null
                        ? result.getOrderId().getValue() : null)
                .status(result.getStatus())
                .message(result.getMessage())
                .build();
    }

    public OrderBookResponse toResponse(OrderBookSnapshot snapshot) {
        List<OrderBookResponse.PriceLevelResponse> bids = snapshot.getBids()
                .stream()
                .map(level -> OrderBookResponse.PriceLevelResponse.builder()
                        .price(level.getPrice())
                        .quantity(level.getQuantity())
                        .orderCount(level.getOrderCount())
                        .build())
                .toList();

        List<OrderBookResponse.PriceLevelResponse> asks = snapshot.getAsks()
                .stream()
                .map(level -> OrderBookResponse.PriceLevelResponse.builder()
                        .price(level.getPrice())
                        .quantity(level.getQuantity())
                        .orderCount(level.getOrderCount())
                        .build())
                .toList();

        return OrderBookResponse.builder()
                .bids(bids)
                .asks(asks)
                .spread(snapshot.getSpread())
                .generatedAt(snapshot.getGeneratedAt())
                .build();
    }
}
