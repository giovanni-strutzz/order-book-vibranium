package com.br.strutz.order_book.domain.port.output;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;

import java.time.Instant;
import java.util.List;

public interface AuditRepository {

    AuditDocument save(AuditDocument document);
    List<AuditDocument> findByCorrelationId(String correlationId);
    List<AuditDocument> findByUserIdOrderByOccurredAtDesc(String userId, int page, int size);
    List<AuditDocument> findByOccurredAtBetween(Instant from, Instant to);
}
