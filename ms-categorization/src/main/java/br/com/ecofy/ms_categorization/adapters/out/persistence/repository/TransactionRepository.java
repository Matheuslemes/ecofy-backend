package br.com.ecofy.ms_categorization.adapters.out.persistence.repository;

import br.com.ecofy.ms_categorization.adapters.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByImportJobIdAndExternalId(UUID importJobId, String externalId);

}
