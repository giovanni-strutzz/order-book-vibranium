package com.br.strutz.order_book.adapter.input.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
@Schema(description = "Snapshot do livro de ofertas")
public class OrderBookResponse {

    @Schema(description = "Ordens de compra ordenadas por preço decrescente")
    List<PriceLevelResponse> bids;

    @Schema(description = "Ordens de venda ordenadas por preço crescente")
    List<PriceLevelResponse> asks;

    @Schema(description = "Spread entre melhor ask e melhor bid", example = "0.50")
    BigDecimal spread;

    @Schema(description = "Momento de geração do snapshot")
    Instant generatedAt;

    @Value
    @Builder
    @Schema(description = "Nível de preço agregado")
    public static class PriceLevelResponse {

        @Schema(description = "Preço do nível", example = "150.00")
        BigDecimal price;

        @Schema(description = "Quantidade total neste nível", example = "25.5")
        BigDecimal quantity;

        @Schema(description = "Número de ordens neste nível", example = "3")
        int orderCount;
    }
}