package br.com.ecofy.ms_budgeting.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "budget_consumption")
public class BudgetConsumptionEntity {

    @Id
    private UUID id;

    @Column(name = "budget_id", nullable = false)
    private UUID budgetId;

    @Column(name = "consumed_cents", nullable = false)
    private long consumedCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "source", nullable = false, length = 40)
    private String source;

    @Column(name = "last_transaction_id")
    private UUID lastTransactionId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}