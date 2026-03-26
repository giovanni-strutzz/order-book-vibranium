package com.br.strutz.order_book.domain.port.output.audit;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;
import com.br.strutz.order_book.domain.model.AuditEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

public interface AuditRepository {

    AuditEntry save(AuditEntry entry);

    List<AuditEntry> findByCorrelationId(String correlationId);

    List<AuditEntry> findByUserId(String userId, int page, int size);

    List<AuditEntry> findByOccurredAtBetween(Instant from, Instant to);
}
