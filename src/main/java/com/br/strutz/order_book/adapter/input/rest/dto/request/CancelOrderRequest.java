package com.br.strutz.order_book.adapter.input.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requisição para cancelar uma ordem")
public class CancelOrderRequest {

    @NotBlank
    @Schema(description = "ID do usuário dono da ordem", example = "user-123")
    private String userId;
}
