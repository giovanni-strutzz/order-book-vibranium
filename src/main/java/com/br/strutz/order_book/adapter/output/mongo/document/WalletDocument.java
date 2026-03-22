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
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "wallets")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_id",
                def = "{'userId': 1}", unique = true)
})
public class WalletDocument {

    @Version
    private Long version;

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("available_balance")
    private BigDecimal availableBalance;

    @Field("reserved_balance")
    private BigDecimal reservedBalance;

    @Field("transactions")
    private List<WalletTransactionEmbedded> transactions = new ArrayList<>();

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WalletTransactionEmbedded {

        private String type;
        private BigDecimal amount;
        private String description;

        @Field("occurred_at")
        private Instant occurredAt;
    }
}
