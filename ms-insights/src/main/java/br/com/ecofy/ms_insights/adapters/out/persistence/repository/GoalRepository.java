package br.com.ecofy.ms_insights.adapters.out.persistence.repository;

import br.com.ecofy.ms_insights.adapters.out.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    List<GoalEntity> findTop200ByUserIdOrderByUpdatedAtDesc(UUID userId);
}
