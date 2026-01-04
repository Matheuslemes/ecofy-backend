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
@Table(name = "budget_alert")
public class BudgetAlertEntity {

    @Id
    private UUID id;

    @Column(name = "budget_id", nullable = false)
    private UUID budgetId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "threshold_percent")
    private Integer thresholdPercent;

    @Column(name = "consumed_cents", nullable = false)
    private long consumedCents;

    @Column(name = "limit_cents", nullable = false)
    private long limitCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}