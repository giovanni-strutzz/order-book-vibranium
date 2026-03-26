package com.br.strutz.order_book.adapter.input.rest.swagger;

import com.br.strutz.order_book.adapter.input.rest.dto.response.WalletResponse;
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

@Tag(name = "Wallet", description = "Consulta de saldo e histórico da carteira")
@RequestMapping("/api/v1/wallets")
public interface WalletSwagger {

    @Operation(
            summary = "Consultar carteira",
            description = "Retorna saldo disponível, reservado e total do usuário, " +
                    "incluindo quantidade de Vibranium.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carteira retornada",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    @GetMapping("/{userId}")
    ResponseEntity<WalletResponse> getWallet(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable String userId);

    @Operation(
            summary = "Histórico de transações",
            description = "Retorna o histórico de créditos, débitos e reservas da carteira.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    @GetMapping("/{userId}/transactions")
    ResponseEntity<?> getTransactionHistory(
            @PathVariable String userId,
            @Parameter(description = "Página") int page,
            @Parameter(description = "Tamanho da página") int size);
}
