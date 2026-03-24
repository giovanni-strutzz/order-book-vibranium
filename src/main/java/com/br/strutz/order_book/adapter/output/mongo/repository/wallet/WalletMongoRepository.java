package com.br.strutz.order_book.adapter.output.mongo.repository.wallet;

import com.br.strutz.order_book.adapter.output.mongo.document.WalletDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletMongoRepository extends MongoRepository<WalletDocument, String> {

    Optional<WalletDocument> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
