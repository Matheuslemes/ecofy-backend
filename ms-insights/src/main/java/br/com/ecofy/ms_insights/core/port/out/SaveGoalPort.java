package br.com.ecofy.ms_insights.core.port.out;

import br.com.ecofy.ms_insights.core.domain.Goal;

public interface SaveGoalPort {
    Goal save(Goal goal);
}
