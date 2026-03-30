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

    private final WalletMongoRepository mongoRepository;
    private final WalletMapper          mapper;

    public WalletAdapter(WalletMongoRepository mongoRepository,
                         WalletMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper          = mapper;
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletDocument saved = mongoRepository.save(mapper.toDocument(wallet));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Wallet> findByUserId(UserId userId) {
        return mongoRepository.findByUserId(userId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return mongoRepository.existsByUserId(userId.getValue());
    }
}