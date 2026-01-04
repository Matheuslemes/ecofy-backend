package br.com.ecofy.ms_budgeting.core.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record RecalculateBudgetsCommand (

        UUID userId,
        LocalDate referenceDate

) { }