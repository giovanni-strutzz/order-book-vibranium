package com.br.strutz.order_book.adapter.input.rest.mapper;

import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletResponse;
import com.br.strutz.order_book.application.query.WalletSnapshot;
import org.springframework.stereotype.Component;

@Component
public class WalletRestMapper {

    public WalletResponse toResponse(WalletSnapshot snapshot) {
        return WalletResponse.builder()
                .userId(snapshot.getUserId())
                .availableBalance(snapshot.getAvailableBalance())
                .reservedBalance(snapshot.getReservedBalance())
                .totalBalance(snapshot.getTotalBalance())
                .updatedAt(snapshot.getUpdatedAt())
                .build();
    }
}
