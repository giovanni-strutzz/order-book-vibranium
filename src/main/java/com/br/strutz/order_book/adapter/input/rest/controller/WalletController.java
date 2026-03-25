package com.br.strutz.order_book.adapter.input.rest.controller;

import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletResponse;
import com.br.strutz.order_book.adapter.input.rest.mapper.WalletRestMapper;
import com.br.strutz.order_book.adapter.input.rest.swagger.WalletSwagger;
import com.br.strutz.order_book.adapter.output.mongo.mapper.WalletMapper;
import com.br.strutz.order_book.application.query.GetWalletQuery;
import com.br.strutz.order_book.application.query.WalletSnapshot;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.input.GetWalletUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController implements WalletSwagger {

    private final GetWalletUseCase getWalletUseCase;
    private final WalletRestMapper mapper;

    public WalletController(GetWalletUseCase getWalletUseCase,
                            WalletRestMapper mapper) {
        this.getWalletUseCase = getWalletUseCase;
        this.mapper           = mapper;
    }

    @Override
    public ResponseEntity<WalletResponse> getWallet(String userId) {
        WalletSnapshot snapshot = getWalletUseCase
                .handle(GetWalletQuery.of(UserId.of(userId)));
        return ResponseEntity.ok(mapper.toResponse(snapshot));
    }

    @Override
    public ResponseEntity<?> getTransactionHistory(String userId,
                                                   int page, int size) {
        return ResponseEntity.ok().build();
    }
}