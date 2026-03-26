package com.br.strutz.order_book.application.query;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class OrderSnapshot {
    String orderId;
    String userId;
    String orderType;
    BigDecimal price;
    BigDecimal quantity;
    String status;
    Instant createdAt;
    Instant updatedAt;
    String correlationId;
}
