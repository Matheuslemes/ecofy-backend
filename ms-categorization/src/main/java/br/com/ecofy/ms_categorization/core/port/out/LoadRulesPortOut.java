package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.core.domain.CategorizationRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadRulesPortOut {

    List<CategorizationRule> findActiveOrdered();
    Optional<CategorizationRule> findById(UUID id);

}
