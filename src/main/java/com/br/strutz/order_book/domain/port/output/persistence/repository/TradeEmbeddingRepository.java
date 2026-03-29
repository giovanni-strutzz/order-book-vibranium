package com.br.strutz.order_book.domain.port.output.persistence.repository;

import com.br.strutz.order_book.domain.port.output.persistence.entities.TradeEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeEmbeddingRepository extends JpaRepository<TradeEmbeddingEntity, UUID> {

    boolean existsByTradeId(String tradeId);

    List<TradeEmbeddingEntity> findByBuyerId(String buyerId);

    List<TradeEmbeddingEntity> findBySellerId(String sellerId);

    @Query(value = """
            SELECT *
            FROM trade_embeddings
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<TradeEmbeddingEntity> findTopKSimilar(
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    @Query(value = """
            SELECT *
            FROM trade_embeddings
            WHERE buyer_id = :userId
               OR seller_id = :userId
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<TradeEmbeddingEntity> findTopKSimilarByUserId(
            @Param("embedding") String embedding,
            @Param("userId") String userId,
            @Param("limit") int limit);
}
