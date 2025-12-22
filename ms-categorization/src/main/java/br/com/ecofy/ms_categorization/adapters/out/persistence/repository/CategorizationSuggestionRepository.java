package br.com.ecofy.ms_categorization.adapters.out.persistence.repository;

import br.com.ecofy.ms_categorization.adapters.out.persistence.entity.CategorizationSuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategorizationSuggestionRepository extends JpaRepository<CategorizationSuggestionEntity, UUID> {

    Optional<CategorizationSuggestionEntity> findTopByTransactionIdOrderByUpdatedAtDesc(UUID transactionId);

}
