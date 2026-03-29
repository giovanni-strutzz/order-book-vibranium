package com.br.strutz.order_book.domain.port.output.persistence.repository;

import com.br.strutz.order_book.domain.port.output.persistence.entities.OrderEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderEmbeddingRepository extends JpaRepository<OrderEmbeddingEntity, UUID> {

    boolean existsByOrderId(String orderId);

    List<OrderEmbeddingEntity> findByUserId(String userId);

    @Query(value = """
            SELECT *
            FROM order_embeddings
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<OrderEmbeddingEntity> findTopKSimilar(
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    @Query(value = """
            SELECT *
            FROM order_embeddings
            WHERE user_id = :userId
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<OrderEmbeddingEntity> findTopKSimilarByUserId(
            @Param("embedding") String embedding,
            @Param("userId") String userId,
            @Param("limit") int limit);
}