package br.com.ecofy.ms_insights.adapters.in.web.dto;

import br.com.ecofy.ms_insights.core.domain.enums.GoalStatus;

public record UpdateGoalRequest(
        String name,
        Long targetCents,
        String currency,
        GoalStatus status
) {}
