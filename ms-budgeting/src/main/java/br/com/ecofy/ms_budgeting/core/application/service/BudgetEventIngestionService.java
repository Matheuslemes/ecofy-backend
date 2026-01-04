package br.com.ecofy.ms_budgeting.core.application.service;

import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;
import br.com.ecofy.ms_budgeting.core.port.in.ProcessTransactionForBudgetUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetEventIngestionService {

    private final ProcessTransactionForBudgetUseCase useCase;

    public void ingest(ProcessTransactionCommand cmd) {
        log.debug("[BudgetEventIngestionService] - [ingest] -> txId={} userId={} categoryId={}",
                cmd.transactionId(), cmd.userId(), cmd.categoryId());
        useCase.process(cmd);
    }

}