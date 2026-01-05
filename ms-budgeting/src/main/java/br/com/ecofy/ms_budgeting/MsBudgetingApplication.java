package br.com.ecofy.ms_budgeting;

import br.com.ecofy.ms_budgeting.config.BudgetingProperties;
import br.com.ecofy.ms_budgeting.adapters.in.sched.BudgetingSchedulingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
        BudgetingProperties.class,
        BudgetingSchedulingProperties.class
})
@SpringBootApplication
public class MsBudgetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsBudgetingApplication.class, args);
    }
}
