package br.com.ecofy.ms_categorization.adapters.in.web;

import br.com.ecofy.ms_categorization.adapters.in.web.dto.response.TransactionResponse;
import br.com.ecofy.ms_categorization.core.domain.Transaction;
import br.com.ecofy.ms_categorization.core.port.in.ListCategorizableTransactionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Expõe as transações categorizáveis do usuário autenticado, dando entrada às
// telas de Categorização Manual e Sugestões no API Mode.
@RestController
@RequestMapping(path = "/api/categorization/v1/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Transactions", description = "Consulta de transações do usuário para categorização")
@Slf4j
@RequiredArgsConstructor
public class TransactionQueryController {

    private final ListCategorizableTransactionsUseCase useCase;
    private final AuthenticatedUser authenticatedUser;

    @Operation(
            summary = "Lista as transações do usuário autenticado",
            description = """
                    Retorna as transações do usuário (dono derivado do JWT).
                    - status=UNMATCHED (padrão): apenas as ainda não categorizadas (categorizáveis).
                    - status=ALL: todas as transações do usuário.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de transações"),
            @ApiResponse(responseCode = "401", description = "Não autenticado (JWT ausente/inválido)")
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(
            @Parameter(description = "Filtro por status: UNMATCHED (padrão) ou ALL")
            @RequestParam(name = "status", required = false) String status
    ) {
        UUID userId = authenticatedUser.currentUserId();

        // Padrão = apenas categorizáveis (UNMATCHED). "ALL" retorna todas.
        boolean onlyUncategorized = status == null || !"ALL".equalsIgnoreCase(status.trim());

        List<Transaction> transactions = useCase.list(userId, onlyUncategorized);

        List<TransactionResponse> body = transactions.stream()
                .map(TransactionResponse::from)
                .toList();

        log.info("[TransactionQueryController] - [list] -> userId={} status={} onlyUncategorized={} total={}",
                userId, status, onlyUncategorized, body.size());

        return ResponseEntity.ok(body);
    }
}
