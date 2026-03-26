package com.br.strutz.order_book.adapter.input.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
@Schema(description = "Snapshot da carteira do usuário")
public class WalletResponse {

    @Schema(description = "ID do usuário", example = "user-123")
    String userId;

    @Schema(description = "Saldo disponível em reais", example = "1000.00")
    BigDecimal availableBalance;

    @Schema(description = "Saldo reservado em ordens abertas", example = "200.00")
    BigDecimal reservedBalance;

    @Schema(description = "Saldo total", example = "1200.00")
    BigDecimal totalBalance;

    @Schema(description = "Última atualização da carteira")
    Instant updatedAt;
}
