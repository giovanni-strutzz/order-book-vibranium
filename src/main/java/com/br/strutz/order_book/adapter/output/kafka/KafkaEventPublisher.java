package com.br.strutz.order_book.adapter.output.kafka;

import com.br.strutz.order_book.adapter.output.kafka.exception.EventPublishException;
import com.br.strutz.order_book.domain.event.*;
import com.br.strutz.order_book.domain.port.output.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaEventPublisher.class);

    // Timeout para publishAndWait — evita bloqueio indefinido
    private static final long PUBLISH_TIMEOUT_SECONDS = 5L;

    // Mapeamento evento → tópico Kafka
    private static final Map<Class<? extends Event>, String> TOPIC_MAP = Map.of(
            OrderPlacedEvent.class,    "orders.placed",
            OrderMatchedEvent.class,   "orders.matched",
            BalanceUpdatedEvent.class, "balance.updated",
            OrderCancelledEvent.class, "orders.cancelled"
    );

    private static final String AUDIT_TOPIC = "audit.log";

    private final KafkaTemplate<String, Event> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Event> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Event event) {
        String topic = resolveTopic(event);

        // correlationId como key — garante ordering por ordem no mesmo tópico
        kafkaTemplate.send(topic, event.getCorrelationId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event — topic={} correlationId={} error={}",
                                topic, event.getCorrelationId(), ex.getMessage());
                    } else {
                        log.debug("Event published — topic={} partition={} offset={} correlationId={}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                event.getCorrelationId());
                    }
                });

        publishToAuditLog(event);
    }

    @Override
    public void publishAndWait(Event event) {
        String topic = resolveTopic(event);

        try {
            SendResult<String, Event> result = kafkaTemplate
                    .send(topic, event.getCorrelationId(), event)
                    .get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.debug("Event published synchronously — topic={} partition={} offset={} correlationId={}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.getCorrelationId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException(
                    "Interrupted while publishing event — correlationId: "
                            + event.getCorrelationId(), e);

        } catch (ExecutionException e) {
            throw new EventPublishException(
                    "Failed to publish event — correlationId: "
                            + event.getCorrelationId(), e);

        } catch (TimeoutException e) {
            throw new EventPublishException(
                    "Timeout publishing event after %ds — correlationId: %s"
                            .formatted(PUBLISH_TIMEOUT_SECONDS,
                                    event.getCorrelationId()), e);
        }

        publishToAuditLog(event);
    }

    private void publishToAuditLog(Event event) {
        kafkaTemplate.send(AUDIT_TOPIC, event.getCorrelationId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish to audit log — correlationId={} error={}",
                                event.getCorrelationId(), ex.getMessage());
                    }
                });
    }

    private String resolveTopic(Event event) {
        String topic = TOPIC_MAP.get(event.getClass());
        if (topic == null) {
            log.warn("No topic mapped for event {} — routing to audit.log",
                    event.getClass().getSimpleName());
            return AUDIT_TOPIC;
        }
        return topic;
    }
}