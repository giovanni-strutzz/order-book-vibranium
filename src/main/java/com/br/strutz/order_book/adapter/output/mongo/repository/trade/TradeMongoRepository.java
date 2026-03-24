package com.br.strutz.order_book.adapter.output.mongo.repository.trade;

import com.br.strutz.order_book.adapter.output.mongo.document.TradeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TradeMongoRepository extends MongoRepository<TradeDocument, String> {

    List<TradeDocument> findByExecutedAtBetween(Instant from, Instant to);
}