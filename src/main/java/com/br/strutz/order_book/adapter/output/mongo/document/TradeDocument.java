package com.br.strutz.order_book.adapter.output.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
@Document(collection = "trades")
@CompoundIndexes({
        @CompoundIndex(name = "idx_buy_order",
                def = "{'buyOrderId': 1}"),
        @CompoundIndex(name = "idx_sell_order",
                def = "{'sellOrderId': 1}"),
        @CompoundIndex(name = "idx_buyer",
                def = "{'buyerId': 1, 'executedAt': -1}"),
        @CompoundIndex(name = "idx_seller",
                def = "{'sellerId': 1, 'executedAt': -1}"),
        @CompoundIndex(name = "idx_correlation",
                def = "{'correlationId': 1}")
})
public class TradeDocument {

    private BigDecimal price;
    private BigDecimal quantity;

    @Id
    private String id;

    @Field("buy_order_id")
    private String buyOrderId;

    @Field("sell_order_id")
    private String sellOrderId;

    @Field("buyer_id")
    private String buyerId;

    @Field("seller_id")
    private String sellerId;

    @Field("total_value")
    private BigDecimal totalValue;

    @Field("executed_at")
    private Instant executedAt;

    @Field("correlation_id")
    private String correlationId;
}
