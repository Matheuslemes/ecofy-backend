package br.com.ecofy.ms_budgeting.core.port.out;

import java.time.LocalDate;

public interface DeleteArchivedBudgetsOlderThanPort {

    long deleteArchivedBudgetsOlderThan(LocalDate cutoffDateInclusive);

}
