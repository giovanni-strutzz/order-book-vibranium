package com.br.strutz.order_book.adapter.output.mongo.adapter;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.AuditMapper;
import com.br.strutz.order_book.adapter.output.mongo.repository.audit.AuditMongoRepository;
import com.br.strutz.order_book.domain.model.AuditEntry;
import com.br.strutz.order_book.domain.port.output.audit.AuditRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class AuditAdapter implements AuditRepository {

    private final AuditMongoRepository mongoRepository;
    private final AuditMapper          mapper;
    private final MongoTemplate        mongoTemplate;

    public AuditAdapter(AuditMongoRepository mongoRepository,
                        AuditMapper mapper,
                        MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mapper          = mapper;
        this.mongoTemplate   = mongoTemplate;
    }

    @Override
    public AuditEntry save(AuditEntry entry) {
        AuditDocument toInsert = mapper.toDocument(entry);
        AuditDocument inserted = mongoTemplate.insert(toInsert);
        return mapper.toDomain(inserted);
    }

    @Override
    public List<AuditEntry> findByCorrelationId(String correlationId) {
        return mongoRepository.findByCorrelationId(correlationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditEntry> findByUserId(String userId, int page, int size) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "occurredAt")));

        return mongoTemplate.find(query, AuditDocument.class)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditEntry> findByOccurredAtBetween(Instant from, Instant to) {
        return mongoRepository.findByOccurredAtBetween(from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}