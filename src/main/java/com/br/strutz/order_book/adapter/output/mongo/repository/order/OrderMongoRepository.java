package com.br.strutz.order_book.adapter.output.mongo.repository.order;

import com.br.strutz.order_book.adapter.output.mongo.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderMongoRepository extends MongoRepository<OrderDocument, String> {

    Optional<OrderDocument> findByCorrelationId(String correlationId);

    boolean existsByCorrelationId(String correlationId);

    List<OrderDocument> findByTypeAndStatusIn(String type, List<String> statuses);

    List<OrderDocument> findByUserIdAndStatusInOrderByCreatedAtDesc(String userId, List<String> statuses);
}