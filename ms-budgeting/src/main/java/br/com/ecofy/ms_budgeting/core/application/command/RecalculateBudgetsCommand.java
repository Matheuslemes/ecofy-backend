package br.com.ecofy.ms_budgeting.core.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record RecalculateBudgetsCommand(
        UUID runId,
        UUID userId,
        LocalDate referenceDate
) {

    public RecalculateBudgetsCommand {
        if (runId == null) throw new IllegalArgumentException("runId must not be null");
        if (referenceDate == null) throw new IllegalArgumentException("referenceDate must not be null");
    }

    public static RecalculateBudgetsCommand forAllUsers(UUID runId, LocalDate referenceDate) {
        return new RecalculateBudgetsCommand(runId, null, referenceDate);
    }

    public static RecalculateBudgetsCommand forUser(UUID runId, UUID userId, LocalDate referenceDate) {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
        return new RecalculateBudgetsCommand(runId, userId, referenceDate);
    }

}
