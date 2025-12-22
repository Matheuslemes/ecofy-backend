package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.core.domain.Category;

public interface SaveCategoryPortOut {

    Category save(Category category);

}
