package com.br.strutz.order_book.adapter.output.mongo.mapper;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;
import com.br.strutz.order_book.domain.event.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditMapper {

    @Value("${spring.application.name}")
    private String serviceName;

    public AuditDocument toDocument(Event event,
                                    String kafkaTopic,
                                    Long kafkaOffset,
                                    Integer kafkaPartition) {
        return AuditDocument.builder()
                .id(UUID.randomUUID().toString())
                .correlationId(event.getCorrelationId())
                .eventType(event.getClass().getSimpleName())
                .userId(event.getUserId())
                .payload(toPayloadMap(event))
                .occurredAt(Instant.now())
                .sourceService(serviceName)
                .kafkaTopic(kafkaTopic)
                .kafkaOffset(kafkaOffset)
                .kafkaPartition(kafkaPartition)
                .build();
    }

    private Map<String, Object> toPayloadMap(Event event) {
        return Map.of(
                "eventType",     event.getClass().getSimpleName(),
                "correlationId", event.getCorrelationId(),
                "occurredAt",    event.getOccurredAt().toString(),
                "userId",        event.getUserId() != null ? event.getUserId() : ""
        );
    }
}
