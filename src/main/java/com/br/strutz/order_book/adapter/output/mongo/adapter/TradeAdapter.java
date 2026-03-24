package com.br.strutz.order_book.adapter.output.mongo.adapter;

import com.br.strutz.order_book.adapter.output.mongo.document.TradeDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.TradeMapper;
import com.br.strutz.order_book.adapter.output.mongo.repository.trade.TradeMongoRepository;
import com.br.strutz.order_book.domain.model.TradeId;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.output.trade.TradeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class TradeAdapter implements TradeRepository {

    private final TradeMongoRepository mongoRepository;
    private final TradeMapper mapper;
    private final MongoTemplate mongoTemplate;

    public TradeAdapter(TradeMongoRepository mongoRepository,
                        TradeMapper mapper,
                        MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mapper          = mapper;
        this.mongoTemplate   = mongoTemplate;
    }

    @Override
    public Trade save(Trade trade) {
        TradeDocument saved = mongoRepository.save(mapper.toDocument(trade));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Trade> findById(TradeId id) {
        return mongoRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<Trade> findByUserIdOrderByExecutedAtDesc(UserId userId,
                                                         int page, int size) {
        Query query = new Query(
                new Criteria().orOperator(
                        Criteria.where("buyer_id").is(userId.getValue()),
                        Criteria.where("seller_id").is(userId.getValue())))
                .with(PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "executedAt")));

        return mongoTemplate.find(query, TradeDocument.class)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Trade> findByExecutedAtBetween(Instant from, Instant to) {
        return mongoRepository.findByExecutedAtBetween(from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
