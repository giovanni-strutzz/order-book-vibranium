package com.br.strutz.order_book.adapter.output.mongo.mapper;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;
import com.br.strutz.order_book.domain.event.Event;
import com.br.strutz.order_book.domain.model.AuditEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditMapper {

    @Value("${spring.application.name}")
    private String serviceName;

    public AuditEntry toEntry(Event event,
                              String kafkaTopic,
                              Long kafkaOffset,
                              Integer kafkaPartition) {
        return AuditEntry.builder()
                .id(UUID.randomUUID().toString())
                .correlationId(event.getCorrelationId())
                .eventType(event.getClass().getSimpleName())
                .userId(event.getUserId())
                .payload(buildPayload(event))
                .occurredAt(Instant.now())
                .sourceService(serviceName)
                .kafkaTopic(kafkaTopic)
                .kafkaOffset(kafkaOffset)
                .kafkaPartion(kafkaPartition)
                .build();
    }

    public AuditDocument toDocument(AuditEntry entry) {
        return AuditDocument.builder()
                .id(entry.getId())
                .correlationId(entry.getCorrelationId())
                .eventType(entry.getEventType())
                .userId(entry.getUserId())
                .payload(entry.getPayload())
                .occurredAt(entry.getOccurredAt())
                .sourceService(entry.getSourceService())
                .kafkaTopic(entry.getKafkaTopic())
                .kafkaOffset(entry.getKafkaOffset())
                .kafkaPartition(entry.getKafkaPartion())
                .build();
    }

    public AuditEntry toDomain(AuditDocument doc) {
        return AuditEntry.builder()
                .id(doc.getId())
                .correlationId(doc.getCorrelationId())
                .eventType(doc.getEventType())
                .userId(doc.getUserId())
                .payload(doc.getPayload())
                .occurredAt(doc.getOccurredAt())
                .sourceService(doc.getSourceService())
                .kafkaTopic(doc.getKafkaTopic())
                .kafkaOffset(doc.getKafkaOffset())
                .kafkaPartion(doc.getKafkaPartition())
                .build();
    }

    private Map<String, Object> buildPayload(Event event) {
        return Map.of(
                "eventType",     event.getClass().getSimpleName(),
                "correlationId", event.getCorrelationId(),
                "occurredAt",    event.getOccurredAt().toString(),
                "userId",        event.getUserId() != null ? event.getUserId() : ""
        );
    }
}
