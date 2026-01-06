package br.com.ecofy.ms_insights.adapters.out.persistence.repository;

import br.com.ecofy.ms_insights.adapters.out.persistence.entity.MetricSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetricSnapshotRepository extends JpaRepository<MetricSnapshotEntity, UUID> {
}
