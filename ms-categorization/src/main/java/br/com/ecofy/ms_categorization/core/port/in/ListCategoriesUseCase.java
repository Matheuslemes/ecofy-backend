package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.domain.Category;

import java.util.List;

public interface ListCategoriesUseCase {

    List<Category> listActive();

}