package br.com.ecofy.ms_budgeting.adapters.in.web;

import br.com.ecofy.ms_budgeting.adapters.in.web.dto.BudgetOverviewResponse;
import br.com.ecofy.ms_budgeting.adapters.in.web.dto.BudgetResponse;
import br.com.ecofy.ms_budgeting.adapters.in.web.dto.CreateBudgetRequest;
import br.com.ecofy.ms_budgeting.adapters.in.web.dto.UpdateBudgetRequest;
import br.com.ecofy.ms_budgeting.core.application.command.CreateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.command.DeleteBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.command.UpdateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/api/budgeting/v1/budgets", produces = MediaType.APPLICATION_JSON_VALUE)
public class BudgetController {

    private final CreateBudgetUseCase createBudgetUseCase;
    private final UpdateBudgetUseCase updateBudgetUseCase;
    private final DeleteBudgetUseCase deleteBudgetUseCase;
    private final ListBudgetsUseCase listBudgetsUseCase;
    private final GetBudgetUseCase getBudgetUseCase;
    private final GetBudgetOverviewUseCase getBudgetOverviewUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BudgetResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateBudgetRequest req
    ) {
        var cmd = new CreateBudgetCommand(
                req.userId(),
                req.categoryId(),
                req.periodType(),
                req.periodStart(),
                req.periodEnd(),
                req.limitAmount(),
                req.currency(),
                req.status()
        );

        var created = createBudgetUseCase.create(cmd, idempotencyKey);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(BudgetResponse.from(created));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BudgetResponse> update(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest req
    ) {
        var cmd = new UpdateBudgetCommand(id, req.newLimitAmount(), req.currency(), req.status());
        var updated = updateBudgetUseCase.update(cmd, idempotencyKey);
        return ResponseEntity.ok(BudgetResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID id
    ) {
        deleteBudgetUseCase.delete(new DeleteBudgetCommand(id), idempotencyKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> listByUser(@RequestParam UUID userId) {
        var list = listBudgetsUseCase.listByUser(userId).stream()
                .map(BudgetResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(BudgetResponse.from(getBudgetUseCase.get(id)));
    }

    @GetMapping("/overview")
    public ResponseEntity<BudgetOverviewResponse> overview(@RequestParam UUID userId) {
        return ResponseEntity.ok(BudgetOverviewResponse.from(getBudgetOverviewUseCase.overview(userId)));
    }
}
