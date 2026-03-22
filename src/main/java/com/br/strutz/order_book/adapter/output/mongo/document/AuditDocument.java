package com.br.strutz.order_book.adapter.output.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "audit_log")
@CompoundIndexes({
        @CompoundIndex(name = "idx_correlation",
                def = "{'correlationId': 1}"),
        @CompoundIndex(name = "idx_user_date",
                def = "{'userId': 1, 'occurredAt': -1}"),
        @CompoundIndex(name = "idx_event_type",
                def = "{'eventType': 1, 'occurredAt': -1}")
})
public class AuditDocument {

    @Id
    private String id;

    @Field("correlation_id")
    private String correlationId;

    @Field("event_type")
    private String eventType;

    @Field("user_id")
    private String userId;

    @Field("payload")
    private Map<String, Object> payload;

    @Field("occurred_at")
    private Instant occurredAt;

    @Field("source_service")
    private String sourceService;

    @Field("kafka_topic")
    private String kafkaTopic;

    @Field("kafka_offset")
    private Long kafkaOffset;

    @Field("kafka_partition")
    private Integer kafkaPartition;
}
