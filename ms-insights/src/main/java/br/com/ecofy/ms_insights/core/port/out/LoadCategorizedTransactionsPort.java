package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.valueobject.Period;

import java.util.List;
import java.util.UUID;

public interface LoadCategorizedTransactionsPort {
    List<CategorizedTxView> loadForUserAndPeriod(UUID userId, Period period, int limit);
}
