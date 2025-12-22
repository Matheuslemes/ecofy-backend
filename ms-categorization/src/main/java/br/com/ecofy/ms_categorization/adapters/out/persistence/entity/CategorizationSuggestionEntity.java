package br.com.ecofy.ms_categorization.adapters.out.persistence.entity;

import br.com.ecofy.ms_categorization.core.domain.enums.SuggestionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cat_suggestions", indexes = @Index(name = "idx_cat_sug_tx", columnList = "transaction_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorizationSuggestionEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "transaction_id", columnDefinition = "uuid", nullable = false)
    private UUID transactionId;

    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "rule_id", columnDefinition = "uuid")
    private UUID ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SuggestionStatus status;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "rationale", columnDefinition = "text")
    private String rationale;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}