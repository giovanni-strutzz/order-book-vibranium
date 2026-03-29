package com.br.strutz.order_book.infrastructure.ai;

import com.br.strutz.order_book.domain.event.OrderMatchedEvent;
import com.br.strutz.order_book.domain.event.OrderPlacedEvent;
import com.br.strutz.order_book.domain.port.output.persistence.entities.OrderEmbeddingEntity;
import com.br.strutz.order_book.domain.port.output.persistence.entities.TradeEmbeddingEntity;
import com.br.strutz.order_book.domain.port.output.persistence.repository.OrderEmbeddingRepository;
import com.br.strutz.order_book.domain.port.output.persistence.repository.TradeEmbeddingRepository;
import com.br.strutz.order_book.domain.service.ai.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmbeddingIngestionConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(EmbeddingIngestionConsumer.class);

    private final EmbeddingService embeddingService;
    private final OrderEmbeddingRepository orderEmbeddingRepository;
    private final TradeEmbeddingRepository tradeEmbeddingRepository;

    public EmbeddingIngestionConsumer(EmbeddingService embeddingService,
                                      OrderEmbeddingRepository orderEmbeddingRepository,
                                      TradeEmbeddingRepository tradeEmbeddingRepository) {
        this.embeddingService         = embeddingService;
        this.orderEmbeddingRepository = orderEmbeddingRepository;
        this.tradeEmbeddingRepository = tradeEmbeddingRepository;
    }

    @KafkaListener(
            topics = "order.placed",
            groupId = "rag-ingestion-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Ingesting order embedding — orderId={} userId={}",
                event.getOrderId(), event.getUserId());

        if (orderEmbeddingRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Order embedding already exists — orderId={}", event.getOrderId());
            return;
        }

        String content = buildOrderContent(event);
        float[] embedding = embeddingService.generateEmbedding(content);

        OrderEmbeddingEntity entity = OrderEmbeddingEntity.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .orderType(event.getOrderType())
                .price(new BigDecimal(event.getPrice()))
                .quantity(new BigDecimal(event.getQuantity()))
                .status("PENDING")
                .content(content)
                .embedding(embedding)
                .occurredAt(event.getOccurredAt())
                .build();

        orderEmbeddingRepository.save(entity);

        log.info("Order embedding saved — orderId={}", event.getOrderId());
    }

    @KafkaListener(
            topics = "order.matched",
            groupId = "rag-ingestion-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderMatched(OrderMatchedEvent event) {
        log.info("Ingesting trade embedding — tradeId={}", event.getTradeId());

        if (tradeEmbeddingRepository.existsByTradeId(event.getTradeId())) {
            log.warn("Trade embedding already exists — tradeId={}", event.getTradeId());
            return;
        }

        String content   = buildTradeContent(event);
        float[] embedding = embeddingService.generateEmbedding(content);

        TradeEmbeddingEntity entity = TradeEmbeddingEntity.builder()
                .tradeId(event.getTradeId())
                .buyerId(event.getUserId())
                .sellerId(event.getSellOrderId())
                .price(new BigDecimal(event.getPrice()))
                .quantity(new BigDecimal(event.getQuantity()))
                .totalValue(new BigDecimal(event.getTotalValue()))
                .content(content)
                .embedding(embedding)
                .occurredAt(event.getOccurredAt())
                .build();

        tradeEmbeddingRepository.save(entity);

        log.info("Trade embedding saved — tradeId={}", event.getTradeId());
    }

    private String buildOrderContent(OrderPlacedEvent event) {
        return """
                Order placed by user %s.
                Type: %s.
                Price: %s BRL per VBR.
                Quantity: %s VBR.
                Correlation: %s.
                """.formatted(
                event.getUserId(),
                event.getOrderType(),
                event.getPrice(),
                event.getQuantity(),
                event.getCorrelationId()
        );
    }

    private String buildTradeContent(OrderMatchedEvent event) {
        return """
                Trade executed between buyer %s and seller order %s.
                Price: %s BRL per VBR.
                Quantity: %s VBR.
                Total value: %s BRL.
                Trade ID: %s.
                """.formatted(
                event.getUserId(),
                event.getSellOrderId(),
                event.getPrice(),
                event.getQuantity(),
                event.getTotalValue(),
                event.getTradeId()
        );
    }
}
