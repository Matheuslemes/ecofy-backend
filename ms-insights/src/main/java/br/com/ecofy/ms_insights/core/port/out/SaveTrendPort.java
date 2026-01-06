package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.Trend;

public interface SaveTrendPort {
    Trend save(Trend trend);
}
