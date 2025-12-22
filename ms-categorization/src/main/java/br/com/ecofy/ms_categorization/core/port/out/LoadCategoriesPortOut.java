package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.core.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadCategoriesPortOut {

    List<Category> findActive();
    Optional<Category> findById(UUID id);


}
