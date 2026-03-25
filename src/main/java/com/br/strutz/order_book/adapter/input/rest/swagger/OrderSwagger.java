package com.br.strutz.order_book.adapter.input.rest.swagger;

import com.br.strutz.order_book.adapter.input.rest.dto.request.CancelOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.request.PlaceOrderRequest;
import com.br.strutz.order_book.adapter.input.rest.dto.response.OrderBookResponse;
import com.br.strutz.order_book.adapter.input.rest.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Orders", description = "Gerenciamento de ordens de compra e venda de Vibranium")
@RequestMapping("/api/v1/orders")
public interface OrderSwagger {

    @Operation(
            summary = "Colocar uma ordem",
            description = "Cria uma nova ordem de compra ou venda de Vibranium. " +
                    "A ordem é processada assincronamente pelo matching engine.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Ordem aceita para processamento",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "409", description = "Ordem duplicada — correlationId já processado"),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente")
    })
    @PostMapping
    ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @Parameter(description = "ID único da requisição para idempotência", required = true)
            @RequestHeader("X-Correlation-Id") String correlationId);

    @Operation(
            summary = "Cancelar uma ordem",
            description = "Cancela uma ordem ativa. Apenas o dono da ordem pode cancelá-la.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem cancelada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada"),
            @ApiResponse(responseCode = "409", description = "Ordem já finalizada ou cancelada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para cancelar esta ordem")
    })
    @DeleteMapping("/{orderId}")
    ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "ID da ordem a cancelar", required = true)
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request,
            @RequestHeader("X-Correlation-Id") String correlationId);

    @Operation(
            summary = "Consultar o Order Book",
            description = "Retorna o snapshot atual do livro de ofertas com bids e asks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Snapshot do book retornado",
                    content = @Content(schema = @Schema(implementation = OrderBookResponse.class))),
    })
    @GetMapping("/book")
    ResponseEntity<OrderBookResponse> getOrderBook(
            @Parameter(description = "Profundidade do book — número de níveis de preço")
            @RequestParam(defaultValue = "20") int depth);

    @Operation(
            summary = "Histórico de ordens do usuário",
            description = "Retorna as ordens de um usuário paginadas, da mais recente para a mais antiga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/history/{userId}")
    ResponseEntity<?> getOrderHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);
}
