package com.br.strutz.order_book.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class AuditEntry {

    String id;
    String correlationId;
    String eventType;
    String userId;
    Map<String, Object> payload;
    Instant occurredAt;
    String sourceService;
    String kafkaTopic;
    Long kafkaOffset;
    Integer kafkaPartion;
}
