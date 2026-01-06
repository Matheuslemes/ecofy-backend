package br.com.ecofy.ms_insights.adapters.out.persistence;

import br.com.ecofy.ms_insights.adapters.out.persistence.mapper.MetricSnapshotMapper;
import br.com.ecofy.ms_insights.adapters.out.persistence.repository.MetricSnapshotRepository;
import br.com.ecofy.ms_insights.core.domain.MetricSnapshot;
import br.com.ecofy.ms_insights.core.port.out.SaveMetricSnapshotPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Component
public class MetricSnapshotJpaAdapter implements SaveMetricSnapshotPort {

    private final MetricSnapshotRepository repository;

    public MetricSnapshotJpaAdapter(MetricSnapshotRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    @Transactional
    public MetricSnapshot save(MetricSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        Instant startedAt = Instant.now();

        try {
            var entity = MetricSnapshotMapper.toEntity(snapshot);
            var saved = repository.save(entity);

            log.debug("[MetricSnapshotJpaAdapter] - [save] -> OK snapshotId={} userId={} metricType={} start={} end={} granularity={} valueCents={} currency={} elapsedMs={}",
                    saved.getId(),
                    saved.getUserId(),
                    saved.getMetricType(),
                    saved.getPeriodStart(),
                    saved.getPeriodEnd(),
                    saved.getGranularity(),
                    saved.getValueCents(),
                    saved.getCurrency(),
                    elapsedMs(startedAt)
            );

            return MetricSnapshotMapper.toDomain(saved);

        } catch (DataAccessException ex) {
            log.error("[MetricSnapshotJpaAdapter] - [save] -> DB_ERROR snapshotId={} userId={} metricType={} msg={}",
                    safeSnapshotId(snapshot), safeUserId(snapshot), safeMetricType(snapshot), ex.getMessage(), ex);
            throw ex;

        } catch (RuntimeException ex) {
            log.error("[MetricSnapshotJpaAdapter] - [save] -> ERROR snapshotId={} userId={} metricType={} msg={}",
                    safeSnapshotId(snapshot), safeUserId(snapshot), safeMetricType(snapshot), ex.getMessage(), ex);
            throw ex;
        }
    }

    private static long elapsedMs(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private static Object safeSnapshotId(MetricSnapshot s) {
        try { return s.getId(); } catch (Exception ignore) { return "n/a"; }
    }

    private static Object safeUserId(MetricSnapshot s) {
        try { return s.getUserId().value(); } catch (Exception ignore) { return "n/a"; }
    }

    private static Object safeMetricType(MetricSnapshot s) {
        try { return s.getMetricType(); } catch (Exception ignore) { return "n/a"; }
    }
}
