package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.MetricSnapshot;

import java.util.List;
import java.util.UUID;

public interface LoadMetricSnapshotsPort {

    List<MetricSnapshot> findRecentForUser(UUID userId, int limit);

}
