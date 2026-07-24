package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.core.domain.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadTransactionPortOut {

    Optional<Transaction> findById(UUID id);
    Optional<Transaction> findByExternalKey(UUID importJobId, String externalId);
    List<Transaction> listByUser(UUID userId, boolean onlyUncategorized);

}
