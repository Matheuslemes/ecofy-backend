package br.com.ecofy.ms_insights.core.port.in;

import br.com.ecofy.ms_insights.core.application.command.UpdateGoalCommand;
import br.com.ecofy.ms_insights.core.application.result.GoalResult;

public interface UpdateGoalUseCase {
    GoalResult update(UpdateGoalCommand cmd);
}
