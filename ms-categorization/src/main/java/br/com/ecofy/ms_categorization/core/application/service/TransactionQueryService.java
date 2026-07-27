package br.com.ecofy.ms_categorization.core.application.service;

import br.com.ecofy.ms_categorization.core.domain.Transaction;
import br.com.ecofy.ms_categorization.core.port.in.ListCategorizableTransactionsUseCase;
import br.com.ecofy.ms_categorization.core.port.out.LoadTransactionPortOut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Consulta de transações do usuário para categorização manual/sugestões.
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionQueryService implements ListCategorizableTransactionsUseCase {

    private final LoadTransactionPortOut loadTransactionPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> list(UUID userId, boolean onlyUncategorized) {
        Objects.requireNonNull(userId, "userId must not be null");

        List<Transaction> transactions = loadTransactionPortOut.listByUser(userId, onlyUncategorized);

        log.debug("[TransactionQueryService] - [list] -> userId={} onlyUncategorized={} total={}",
                userId, onlyUncategorized, transactions.size());

        return transactions;
    }
}
