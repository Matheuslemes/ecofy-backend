package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.Goal;

import java.util.List;
import java.util.UUID;

public interface LoadGoalsPort {
    Goal findById(UUID goalId);
    List<Goal> findByUserId(UUID userId);
}
