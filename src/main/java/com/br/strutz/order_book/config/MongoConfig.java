package com.br.strutz.order_book.config;

import com.br.strutz.order_book.adapter.output.mongo.document.AuditDocument;
import com.br.strutz.order_book.adapter.output.mongo.document.OrderDocument;
import com.br.strutz.order_book.adapter.output.mongo.document.TradeDocument;
import com.br.strutz.order_book.adapter.output.mongo.document.WalletDocument;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {

        
        mongoTemplate.indexOps(OrderDocument.class)
                .ensureIndex(new Index()
                        .on("correlation_id", Sort.Direction.ASC)
                        .unique()
                        .sparse()
                        .named("idx_correlation"));

        mongoTemplate.indexOps(OrderDocument.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("user_id", 1).append("created_at", -1))
                        .named("idx_user_date"));

        mongoTemplate.indexOps(OrderDocument.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("status", 1).append("type", 1).append("price", 1))
                        .named("idx_status_type_price"));

        
        mongoTemplate.indexOps(WalletDocument.class)
                .ensureIndex(new Index()
                        .on("user_id", Sort.Direction.ASC)
                        .unique()
                        .named("idx_wallet_user"));

        
        mongoTemplate.indexOps(TradeDocument.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("buyer_id", 1).append("executed_at", -1))
                        .named("idx_trade_buyer"));

        mongoTemplate.indexOps(TradeDocument.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("seller_id", 1).append("executed_at", -1))
                        .named("idx_trade_seller"));

        mongoTemplate.indexOps(TradeDocument.class)
                .ensureIndex(new Index()
                        .on("correlation_id", Sort.Direction.ASC)
                        .named("idx_trade_correlation"));

        
        mongoTemplate.indexOps(AuditDocument.class)
                .ensureIndex(new Index()
                        .on("correlation_id", Sort.Direction.ASC)
                        .named("idx_audit_correlation"));

        mongoTemplate.indexOps(AuditDocument.class)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document("user_id", 1).append("occurred_at", -1))
                        .named("idx_audit_user"));
    }
}