package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.domain.CategorizationRule;

import java.util.List;

public interface ListRulesUseCase {

    List<CategorizationRule> listActive();

}