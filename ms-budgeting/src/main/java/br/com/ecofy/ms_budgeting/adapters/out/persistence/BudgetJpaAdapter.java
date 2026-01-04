package br.com.ecofy.ms_budgeting.adapters.out.persistence;

import br.com.ecofy.ms_budgeting.adapters.out.persistence.mapper.BudgetMapper;
import br.com.ecofy.ms_budgeting.adapters.out.persistence.repository.BudgetRepository;
import br.com.ecofy.ms_budgeting.core.domain.Budget;
import br.com.ecofy.ms_budgeting.core.port.out.LoadBudgetsPort;
import br.com.ecofy.ms_budgeting.core.port.out.SaveBudgetPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BudgetJpaAdapter implements SaveBudgetPort, LoadBudgetsPort {

    private final BudgetRepository repo;

    public BudgetJpaAdapter(BudgetRepository repo) {
        this.repo = repo;
    }

    @Override
    public Budget save(Budget budget) {
        var saved = repo.save(BudgetMapper.toEntity(budget));
        return BudgetMapper.toDomain(saved);
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return repo.findById(id).map(BudgetMapper::toDomain);
    }

    @Override
    public List<Budget> findByUserId(UUID userId) {
        return repo.findByUserId(userId).stream().map(BudgetMapper::toDomain).toList();
    }

    @Override
    public boolean existsByNaturalKey(String naturalKey) {
        return repo.existsByNaturalKey(naturalKey);
    }

}