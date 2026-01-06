package br.com.ecofy.ms_insights.adapters.out.persistence.repository;

import br.com.ecofy.ms_insights.adapters.out.persistence.entity.TrendEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrendRepository extends JpaRepository<TrendEntity, UUID> {
}
