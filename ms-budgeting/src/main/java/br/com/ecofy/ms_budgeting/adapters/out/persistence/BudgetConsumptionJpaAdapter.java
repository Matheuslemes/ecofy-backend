package br.com.ecofy.ms_budgeting.adapters.out.persistence;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper.BudgetConsumptionMapper;
import br.com.ecofy.ms_budgeting.adapters.out.persistence.repository.BudgetConsumptionRepository;
import br.com.ecofy.ms_budgeting.core.domain.BudgetConsumption;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetConsumptionPort;
import br.com.ecofy.ms_budgeting.core.port.out.SaveBudgetConsumptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BudgetConsumptionJpaAdapter implements SaveBudgetConsumptionPort, LoadBudgetConsumptionPort {

    private final BudgetConsumptionRepository repository;

    @Override
    public BudgetConsumption save(BudgetConsumption consumption) {
        var saved = repository.save(BudgetConsumptionMapper.toEntity(consumption));
        return BudgetConsumptionMapper.toDomain(saved);
    }

    @Override
    public BudgetConsumption getCurrentConsumption(UUID budgetId) {
        return repository.findTopByBudgetIdOrderByUpdatedAtDesc(budgetId)
                .map(BudgetConsumptionMapper::toDomain)
                .orElse(null);
    }

}
