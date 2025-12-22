package br.com.ecofy.ms_categorization.core.port.out;


import br.com.ecofy.ms_categorization.core.domain.CategorizationRule;

public interface SaveRulePortOut {

    CategorizationRule save(CategorizationRule rule);

}
