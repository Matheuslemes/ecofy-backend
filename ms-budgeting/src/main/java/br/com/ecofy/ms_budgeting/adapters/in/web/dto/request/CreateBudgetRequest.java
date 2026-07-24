package br.com.ecofy.ms_budgeting.adapters.in.web.dto.request;


import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetPeriodType;
import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBudgetRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID categoryId,

        @NotNull
        BudgetPeriodType periodType,

        @NotNull
        LocalDate periodStart,

        @NotNull
        LocalDate periodEnd,

        @NotNull
        @Min(value = 1)
        Long limitAmountCents,

        @NotBlank
        String currency,
        BudgetStatus status

) { }
