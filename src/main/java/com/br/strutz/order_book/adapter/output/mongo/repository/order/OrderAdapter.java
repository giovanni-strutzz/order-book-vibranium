package com.br.strutz.order_book.adapter.output.mongo.repository.order;

import com.br.strutz.order_book.adapter.output.mongo.document.OrderDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.OrderMapper;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.output.order.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderAdapter implements OrderRepository {

    private static final List<String> ACTIVE_STATUSES =
            List.of("PENDING", "PARTIALLY_FILLED");

    private final OrderMongoRepository mongoRepository;
    private final OrderMapper          mapper;
    private final MongoTemplate        mongoTemplate;

    public OrderAdapter(OrderMongoRepository mongoRepository,
                        OrderMapper mapper,
                        MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mapper          = mapper;
        this.mongoTemplate   = mongoTemplate;
    }

    @Override
    public Order save(Order order) {
        OrderDocument saved = mongoRepository.save(mapper.toDocument(order));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return mongoRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByCorrelationId(String correlationId) {
        return mongoRepository.findByCorrelationId(correlationId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findActiveByType(OrderType type) {
        return mongoRepository
                .findByTypeAndStatusIn(type.name(), ACTIVE_STATUSES)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findActiveByUserId(UserId userId) {
        return mongoRepository
                .findByUserIdAndStatusInOrderByCreatedAtDesc(
                        userId.getValue(), ACTIVE_STATUSES)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByUserIdOrderByCreatedAtDesc(UserId userId,
                                                        int page, int size) {
        Query query = new Query(Criteria.where("userId").is(userId.getValue()))
                .with(PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return mongoTemplate.find(query, OrderDocument.class)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCorrelationId(String correlationId) {
        return mongoRepository.existsByCorrelationId(correlationId);
    }

    @Override
    public long countByUserId(UserId userId) {
        Query query = new Query(Criteria.where("userId").is(userId.getValue()));
        return mongoTemplate.count(query, OrderDocument.class);
    }
}