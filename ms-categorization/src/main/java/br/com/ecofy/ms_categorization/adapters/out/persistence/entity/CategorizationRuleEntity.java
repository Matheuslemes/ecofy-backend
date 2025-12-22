package br.com.ecofy.ms_categorization.adapters.out.persistence.entity;

import br.com.ecofy.ms_categorization.core.domain.enums.RuleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cat_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorizationRuleEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "category_id", columnDefinition = "uuid", nullable = false)
    private UUID categoryId;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RuleStatus status;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "conditions_json", nullable = false, columnDefinition = "text")
    private String conditionsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
