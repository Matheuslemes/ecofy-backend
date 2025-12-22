package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.application.command.ManualCategorizeCommand;
import br.com.ecofy.ms_categorization.core.application.result.CategorizationResult;

public interface ManualCategorizationUseCase {

    CategorizationResult manualCategorize(ManualCategorizeCommand command);

}