package br.com.ecofy.ms_categorization.core.port.in;

import br.com.ecofy.ms_categorization.core.domain.Transaction;

import java.util.List;
import java.util.UUID;

public interface ListCategorizableTransactionsUseCase {

    List<Transaction> list(UUID userId, boolean onlyUncategorized);
}
