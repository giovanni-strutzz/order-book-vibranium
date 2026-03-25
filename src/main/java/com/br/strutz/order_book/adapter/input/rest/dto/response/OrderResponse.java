package com.br.strutz.order_book.adapter.input.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Resposta de operação sobre uma ordem")
public class OrderResponse {

    @Schema(description = "ID da ordem gerada", example = "uuid-123")
    String orderId;

    @Schema(description = "Status da operação", example = "ACCEPTED")
    String status;

    @Schema(description = "Mensagem descritiva", example = "Ordem recebida com sucesso")
    String message;
}