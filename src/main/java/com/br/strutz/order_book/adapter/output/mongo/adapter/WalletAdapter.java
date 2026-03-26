package com.br.strutz.order_book.adapter.output.mongo.adapter;

import com.br.strutz.order_book.adapter.output.mongo.document.WalletDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.WalletMapper;
import com.br.strutz.order_book.adapter.output.mongo.repository.wallet.WalletMongoRepository;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.output.wallet.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WalletAdapter implements WalletRepository {

    private static final Logger log = LoggerFactory.getLogger(WalletAdapter.class);

    private final WalletMongoRepository mongoRepository;
    private final WalletMapper mapper;
    private final MongoTemplate mongoTemplate;

    public WalletAdapter(WalletMongoRepository mongoRepository,
                         WalletMapper mapper,
                         MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mapper          = mapper;
        this.mongoTemplate   = mongoTemplate;
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletDocument document = mapper.toDocument(wallet);

        log.info(">>> saving wallet userId={}", document.getUserId());

        WalletDocument existing = findDocumentByUserId(document.getUserId());

        log.info(">>> existing found={} existingId={}",
                existing != null,
                existing != null ? existing.getId() : "NULL");

        if (existing != null) {
            // Usa replace direto pelo _id — ignora o @Version
            WalletDocument toReplace = WalletDocument.builder()
                    .id(existing.getId())
                    .userId(existing.getUserId())
                    .availableBalance(document.getAvailableBalance())
                    .reservedBalance(document.getReservedBalance())
                    .transactions(document.getTransactions())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(document.getUpdatedAt())
                    .lockTouchedAt(document.getUpdatedAt())
                    .build();

            Query query = new Query(Criteria.where("_id").is(existing.getId()));
            mongoTemplate.findAndReplace(query, toReplace);

            log.info(">>> wallet replaced id={}", existing.getId());
            return mapper.toDomain(toReplace);
        }

        WalletDocument saved = mongoTemplate.insert(document);
        log.info(">>> wallet inserted id={}", saved.getId());
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Wallet> findByUserId(UserId userId) {
        Optional<WalletDocument> doc = mongoRepository.findByUserId(userId.getValue());

        log.info(">>> findByUserId={} found={} docId={}",
                userId.getValue(),
                doc.isPresent(),
                doc.map(WalletDocument::getId).orElse("NOT FOUND"));

        return doc.map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return mongoRepository.existsByUserId(userId.getValue());
    }

    private WalletDocument findDocumentByUserId(String userId) {
        Query query = new Query(Criteria.where("user_id").is(userId));
        return mongoTemplate.findOne(query, WalletDocument.class);
    }
}