package com.br.strutz.order_book.adapter.input.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
@Schema(description = "Histórico de trades do usuário")
public class TradeHistoryResponse {

    List<TradeEntryResponse> trades;
    int    page;
    int    size;
    long   totalElements;

    @Value
    @Builder
    @Schema(description = "Entrada de trade")
    public static class TradeEntryResponse {

        @Schema(description = "ID do trade", example = "uuid-456")
        String tradeId;

        @Schema(description = "Lado do usuário no trade", example = "BUY",
                allowableValues = {"BUY", "SELL"})
        String side;

        @Schema(description = "Preço de execução", example = "150.00")
        BigDecimal price;

        @Schema(description = "Quantidade negociada", example = "5.0")
        BigDecimal quantity;

        @Schema(description = "Valor total", example = "750.00")
        BigDecimal totalValue;

        @Schema(description = "Momento de execução")
        Instant executedAt;

        @Schema(description = "ID de rastreabilidade", example = "corr-789")
        String correlationId;
    }
}
