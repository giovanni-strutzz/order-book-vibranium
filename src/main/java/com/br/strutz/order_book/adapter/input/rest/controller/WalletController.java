package com.br.strutz.order_book.adapter.input.rest.controller;

import com.br.strutz.order_book.adapter.input.rest.dto.request.CreateWalletRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.response.TransactionHistoryResponse;
import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletResponse;
import com.br.strutz.order_book.adapter.input.rest.mapper.WalletRestMapper;
import com.br.strutz.order_book.adapter.input.rest.swagger.WalletSwagger;
import com.br.strutz.order_book.application.command.CreateWalletCommand;
import com.br.strutz.order_book.application.query.GetTransactionHistoryQuery;
import com.br.strutz.order_book.application.query.GetWalletQuery;
import com.br.strutz.order_book.application.query.WalletSnapshot;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.input.CreateWalletUseCase;
import com.br.strutz.order_book.domain.port.input.GetTransactionHistoryUseCase;
import com.br.strutz.order_book.domain.port.input.GetWalletUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class WalletController implements WalletSwagger {

    private final GetWalletUseCase              getWalletUseCase;
    private final CreateWalletUseCase           createWalletUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final WalletRestMapper              mapper;

    public WalletController(GetWalletUseCase getWalletUseCase,
                            CreateWalletUseCase createWalletUseCase,
                            GetTransactionHistoryUseCase getTransactionHistoryUseCase,
                            WalletRestMapper mapper) {
        this.getWalletUseCase             = getWalletUseCase;
        this.createWalletUseCase          = createWalletUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.mapper                       = mapper;
    }

    @Override
    public ResponseEntity<WalletResponse> getWallet(String userId) {
        WalletSnapshot snapshot = getWalletUseCase
                .handle(GetWalletQuery.of(UserId.of(userId)));
        return ResponseEntity.ok(mapper.toResponse(snapshot));
    }

    @Override
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            String userId, int page, int size) {
        TransactionHistoryResponse response = getTransactionHistoryUseCase
                .handle(GetTransactionHistoryQuery.of(UserId.of(userId), page, size));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {

        CreateWalletCommand command = new CreateWalletCommand(
                UserId.of(request.getUserId()),
                Money.of(request.getInitialBalance()));

        Wallet wallet = createWalletUseCase.handle(command);

        WalletSnapshot snapshot = WalletSnapshot.builder()
                .userId(wallet.getUserId().getValue())
                .availableBalance(wallet.getAvailableBalance().getAmount())
                .reservedBalance(wallet.getReservedBalance().getAmount())
                .totalBalance(wallet.totalBalance().getAmount())
                .updatedAt(wallet.getUpdatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(snapshot));
    }
}