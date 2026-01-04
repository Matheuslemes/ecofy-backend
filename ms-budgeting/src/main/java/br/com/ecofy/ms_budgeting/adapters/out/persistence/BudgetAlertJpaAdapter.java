package br.com.ecofy.ms_budgeting.adapters.out.persistence;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper.BudgetAlertMapper;
import br.com.ecofy.ms_budgeting.adapters.out.persistence.repository.BudgetAlertRepository;
import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;
import br.com.ecofy.ms_budgeting.core.port.out.SaveBudgetAlertPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetAlertJpaAdapter implements SaveBudgetAlertPort {

    private final BudgetAlertRepository repository;

    @Override
    public BudgetAlert save(BudgetAlert alert) {
        var saved = repository.save(BudgetAlertMapper.toEntity(alert));
        return BudgetAlertMapper.toDomain(saved);
    }

}
