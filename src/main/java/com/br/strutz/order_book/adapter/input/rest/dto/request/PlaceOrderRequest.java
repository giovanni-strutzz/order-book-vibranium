package com.br.strutz.order_book.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Requisição para colocar uma ordem de compra ou venda")
public class PlaceOrderRequest {

    @NotBlank
    @Schema(description = "ID do usuário", example = "user-123")
    private String userId;

    @NotBlank
    @Schema(description = "Tipo da ordem", example = "BUY", allowableValues = {"BUY", "SELL"})
    private String type;

    @NotNull
    @DecimalMin(value = "0.00000001", message = "Preço deve ser maior que zero")
    @Schema(description = "Preço unitário em reais", example = "150.00")
    private BigDecimal price;

    @NotNull
    @DecimalMin(value = "0.00000001", message = "Quantidade deve ser maior que zero")
    @Schema(description = "Quantidade de Vibranium", example = "10.5")
    private BigDecimal quantity;
}
