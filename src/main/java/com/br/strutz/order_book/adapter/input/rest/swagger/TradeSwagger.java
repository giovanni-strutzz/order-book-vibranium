package com.br.strutz.order_book.adapter.input.rest.swagger;

import com.br.strutz.order_book.adapter.input.rest.dto.response.TradeHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Trades", description = "Histórico de trades executados")
@RequestMapping("/api/v1/trades")
public interface TradeSwagger {

    @Operation(
            summary = "Histórico de trades do usuário",
            description = "Retorna todos os trades executados por um usuário, " +
                    "como comprador ou vendedor, paginados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado",
                    content = @Content(schema = @Schema(implementation = TradeHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{userId}")
    ResponseEntity<TradeHistoryResponse> getTradeHistory(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable String userId,
            @Parameter(description = "Página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int size);
}