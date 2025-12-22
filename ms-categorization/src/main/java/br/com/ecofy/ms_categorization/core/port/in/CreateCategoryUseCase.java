package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.application.command.CreateCategoryCommand;
import br.com.ecofy.ms_categorization.core.domain.Category;

public interface CreateCategoryUseCase {

    Category create(CreateCategoryCommand command);

}