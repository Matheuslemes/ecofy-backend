package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.core.domain.CategorizationSuggestion;

import java.util.Optional;
import java.util.UUID;

public interface SaveSuggestionPortOut {

    CategorizationSuggestion save(CategorizationSuggestion suggestion);
    Optional<CategorizationSuggestion> findByTransactionId(UUID transactionId);

}
