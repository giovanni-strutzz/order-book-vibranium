package com.br.strutz.order_book.adapter.output.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_date",
                def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_status_type_price",
                def = "{'status': 1, 'type': 1, 'price': 1}"),
        @CompoundIndex(name = "idx_correlation",
                def = "{'correlationId': 1}", unique = true)
})
public class OrderDocument {

    @Version
    private Long version;

    @Id
    private String id;
    private String userId;
    private String type;
    private String status;

    @Field("price")
    private BigDecimal price;

    @Field("quantity")
    private BigDecimal quantity;

    @Field("filled_quantity")
    private BigDecimal filledQuantity;

    @Field("total_value")
    private BigDecimal totalValue;

    @Field("correlation_id")
    private String correlationId;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;
}
