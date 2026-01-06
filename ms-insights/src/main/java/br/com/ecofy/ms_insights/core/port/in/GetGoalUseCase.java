package br.com.ecofy.ms_insights.core.port.in;

import br.com.ecofy.ms_insights.core.application.result.GoalResult;

import java.util.UUID;

public interface GetGoalUseCase {
    GoalResult get(UUID goalId);
}
