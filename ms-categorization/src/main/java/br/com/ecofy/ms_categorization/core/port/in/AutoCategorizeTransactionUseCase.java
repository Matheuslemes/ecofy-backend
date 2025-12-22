package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.application.command.AutoCategorizeCommand;
import br.com.ecofy.ms_categorization.core.application.result.CategorizationResult;

public interface AutoCategorizeTransactionUseCase {

    CategorizationResult autoCategorize(AutoCategorizeCommand command);

}
