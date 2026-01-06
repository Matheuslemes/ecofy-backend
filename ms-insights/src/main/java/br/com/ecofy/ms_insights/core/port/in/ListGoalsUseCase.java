package br.com.ecofy.ms_insights.core.port.in;

import br.com.ecofy.ms_insights.core.application.result.GoalResult;

import java.util.List;
import java.util.UUID;

public interface ListGoalsUseCase {
    List<GoalResult> list(UUID userId);
}
