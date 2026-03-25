package com.br.strutz.order_book.adapter.input.rest.controller;

import com.br.strutz.order_book.adapter.input.rest.dto.response.TradeHistoryResponse;
import com.br.strutz.order_book.adapter.input.rest.mapper.TradeRestMapper;
import com.br.strutz.order_book.adapter.input.rest.swagger.TradeSwagger;
import com.br.strutz.order_book.adapter.output.mongo.mapper.TradeMapper;
import com.br.strutz.order_book.application.query.GetTradeHistoryQuery;
import com.br.strutz.order_book.application.query.TradeHistoryResult;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.input.GetTradeHistoryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TradeController implements TradeSwagger {

    private final GetTradeHistoryUseCase getTradeHistoryUseCase;
    private final TradeRestMapper mapper;

    public TradeController(GetTradeHistoryUseCase getTradeHistoryUseCase,
                           TradeRestMapper mapper) {
        this.getTradeHistoryUseCase = getTradeHistoryUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<TradeHistoryResponse> getTradeHistory(String userId,
                                                                int page,
                                                                int size) {
        TradeHistoryResult result = getTradeHistoryUseCase.handle(
                GetTradeHistoryQuery.of(UserId.of(userId), page, size));

        return ResponseEntity.ok(mapper.toResponse(result));
    }
}