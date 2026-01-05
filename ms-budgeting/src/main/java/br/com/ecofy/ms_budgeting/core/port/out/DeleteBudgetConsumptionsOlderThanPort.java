package br.com.ecofy.ms_budgeting.core.port.out;

import java.time.LocalDate;

public interface DeleteBudgetConsumptionsOlderThanPort {

    long deleteConsumptionsOlderThan(LocalDate cutoffDateInclusive);

}
