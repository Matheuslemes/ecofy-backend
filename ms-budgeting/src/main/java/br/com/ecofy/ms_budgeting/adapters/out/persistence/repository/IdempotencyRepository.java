package br.com.ecofy.ms_budgeting.adapters.out.persistence.repository;


import br.com.ecofy.ms_budgeting.adapters.out.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    boolean existsByKey(String key);

}