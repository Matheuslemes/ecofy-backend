package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.MetricSnapshot;

public interface SaveMetricSnapshotPort {
    MetricSnapshot save(MetricSnapshot snapshot);
}
