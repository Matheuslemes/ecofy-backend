package br.com.ecofy.ms_budgeting.core.application.command;

import java.util.UUID;

public record DeleteBudgetCommand (

        UUID budgetId

) { }
