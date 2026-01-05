package br.com.ecofy.ms_budgeting.adapters.in.sched;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ecofy.budgeting.scheduling")
public class BudgetingSchedulingProperties {

    /**
     * Default seguro: false.
     */
    private boolean cleanupEnabled = false;

    /**
     * Política de retenção (dias).
     * Ex.: 90 => apaga dados até (hoje - 90 dias)
     */
    private int cleanupRetentionDays = 90;
}
