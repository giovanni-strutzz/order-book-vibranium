package com.br.strutz.order_book.adapter.output.mongo.repository.audit;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditMongoRepository extends MongoRepository<AuditDocument, String> {

    List<AuditDocument> findByCorrelationId(String correlationId);

    List<AuditDocument> findByOccurredAtBetween(Instant from, Instant to);
}