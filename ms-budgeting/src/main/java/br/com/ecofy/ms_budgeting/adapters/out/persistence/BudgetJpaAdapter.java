package br.com.ecofy.ms_budgeting.adapters.out.persistence;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper.BudgetMapper;
import br.com.ecofy.ms_budgeting.adapters.out.persistence.repository.BudgetRepository;
import br.com.ecofy.ms_budgeting.core.domain.Budget;
import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetsPort;
import br.com.ecofy.ms_budgeting.core.port.out.SaveBudgetPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetJpaAdapter implements SaveBudgetPort, LoadBudgetsPort {

    private final BudgetRepository repo;

    @Override
    @Transactional
    public Budget save(Budget budget) {
        Objects.requireNonNull(budget, "budget must not be null");

        String naturalKey = budget.getKey() != null ? budget.getKey().asNaturalKey() : null;

        log.debug(
                "[BudgetJpaAdapter] - [save] -> id={} naturalKey={}",
                budget.getId(),
                naturalKey
        );

        try {
            var saved = repo.save(BudgetMapper.toEntity(budget));
            return BudgetMapper.toDomain(saved);

        } catch (DataIntegrityViolationException ex) {
            // Mais comum: violação do unique uk_budget_natural (natural_key)
            String causeMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();

            log.warn(
                    "[BudgetJpaAdapter] - [save] -> Data integrity violation. id={} naturalKey={} cause={}",
                    budget.getId(), naturalKey, causeMsg
            );

            // Mantém a causa original para diagnóstico
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Budget> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");

        // Útil em troubleshooting (não gera barulho em prod se log nível >= INFO)
        log.debug("[BudgetJpaAdapter] - [findById] -> id={}", id);

        return repo.findById(id)
                .map(BudgetMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> findByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        log.debug("[BudgetJpaAdapter] - [findByUserId] -> userId={}", userId);

        var entities = repo.findByUserId(userId);
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(BudgetMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNaturalKey(String naturalKey) {
        String nk = requireNonBlank(naturalKey, "naturalKey");

        log.debug("[BudgetJpaAdapter] - [existsByNaturalKey] -> naturalKey={}", nk);

        return repo.existsByNaturalKey(nk);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> findAllActive() {
        log.debug("[BudgetJpaAdapter] - [findAllActive] -> status={}", BudgetStatus.ACTIVE);

        var entities = repo.findByStatus(BudgetStatus.ACTIVE);
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(BudgetMapper::toDomain)
                .toList();
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return trimmed;
    }

}
