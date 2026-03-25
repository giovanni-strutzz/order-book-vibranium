package com.br.strutz.order_book.adapter.input.rest.controller;

import com.br.strutz.order_book.adapter.input.rest.dto.request.CreateWalletRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.request.ReserveBuyOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.request.ReserveSellOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletResponse;
import com.br.strutz.order_book.adapter.input.rest.mapper.WalletRestMapper;
import com.br.strutz.order_book.adapter.input.rest.swagger.WalletSwagger;
import com.br.strutz.order_book.application.command.CreateWalletCommand;
import com.br.strutz.order_book.application.command.PlaceOrderCommand;
import com.br.strutz.order_book.application.query.GetWalletQuery;
import com.br.strutz.order_book.application.query.WalletSnapshot;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.input.CreateWalletUseCase;
import com.br.strutz.order_book.domain.port.input.GetWalletUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class WalletController implements WalletSwagger {

    private final GetWalletUseCase getWalletUseCase;
    private final CreateWalletUseCase createWalletUseCase;
    private final WalletRestMapper mapper;
    private final KafkaTemplate<Object, PlaceOrderCommand> kafkaTemplate;

    public WalletController(GetWalletUseCase getWalletUseCase,
                            CreateWalletUseCase createWalletUseCase,
                            WalletRestMapper mapper, KafkaTemplate<Object, PlaceOrderCommand> kafkaTemplate) {
        this.getWalletUseCase = getWalletUseCase;
        this.createWalletUseCase = createWalletUseCase;
        this.mapper           = mapper;
        this.kafkaTemplate = kafkaTemplate;
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
    };

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {

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

    @PostMapping("/{userId}/reserve-buy")
    public ResponseEntity<?> reserveBuyOrder(@PathVariable String userId,
                                             @Valid @RequestBody ReserveBuyOrderRequest request) {

        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .userId(UserId.of(userId))
                .orderType(OrderType.BUY)
                .price(Money.of(request.getPrice()))
                .quantity(Money.of(request.getQuantity()))
                .correlationId(UUID.randomUUID().toString())
                .build();

        kafkaTemplate.send("wallet-commands", command);

        return ResponseEntity.accepted().build();

    }

    @PostMapping("/{userId}/reserve-sell")
    public ResponseEntity<?> reserveSellOrder(@PathVariable String userId,
                                              @RequestBody ReserveSellOrderRequest request) {

        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .userId(UserId.of(userId))
                .orderType(OrderType.SELL)
                .price(null)
                .quantity(Money.of(request.getQuantity()))
                .correlationId(UUID.randomUUID().toString())
                .build();

        kafkaTemplate.send("wallet-commands", command);

        return ResponseEntity.accepted().build();
    }
}