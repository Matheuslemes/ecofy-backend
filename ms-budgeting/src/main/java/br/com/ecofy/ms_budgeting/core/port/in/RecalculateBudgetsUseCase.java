package br.com.ecofy.ms_budgeting.core.port.in;

import br.com.ecofy.ms_budgeting.core.application.command.RecalculateBudgetsCommand;

public interface RecalculateBudgetsUseCase {

    void recalculate(RecalculateBudgetsCommand cmd);

}