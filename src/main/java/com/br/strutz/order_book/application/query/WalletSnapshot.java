package com.br.strutz.order_book.application.query;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class WalletSnapshot {

    String userId;
    BigDecimal availableBalance;
    BigDecimal reservedBalance;
    BigDecimal totalBalance;
    BigDecimal vibraniumBalance;
    BigDecimal vibraniumReserved;
    Instant updatedAt;
}
