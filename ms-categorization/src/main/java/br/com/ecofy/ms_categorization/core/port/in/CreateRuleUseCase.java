package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.application.command.CreateRuleCommand;
import br.com.ecofy.ms_categorization.core.domain.CategorizationRule;

public interface CreateRuleUseCase {

    CategorizationRule create(CreateRuleCommand command);

}
