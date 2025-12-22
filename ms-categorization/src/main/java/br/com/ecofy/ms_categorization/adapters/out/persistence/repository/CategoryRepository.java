package br.com.ecofy.ms_categorization.adapters.out.persistence.repository;

import br.com.ecofy.ms_categorization.adapters.out.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findByActiveTrueOrderByNameAsc();

    Optional<CategoryEntity> findByNameIgnoreCase(String name);

}
