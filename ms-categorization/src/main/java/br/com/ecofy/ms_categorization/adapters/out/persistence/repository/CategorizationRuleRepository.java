package br.com.ecofy.ms_categorization.adapters.out.persistence.repository;

import br.com.ecofy.ms_categorization.adapters.out.persistence.entity.CategorizationRuleEntity;
import br.com.ecofy.ms_categorization.core.domain.enums.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategorizationRuleRepository extends JpaRepository<CategorizationRuleEntity, UUID> {

    List<CategorizationRuleEntity> findByStatusOrderByPriorityAsc(RuleStatus status);

}
