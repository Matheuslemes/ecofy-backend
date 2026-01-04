package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.command.ProcessTransactionCommand;

public interface ProcessTransactionForBudgetUseCase {

    void process(ProcessTransactionCommand cmd);

}