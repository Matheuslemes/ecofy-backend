package br.com.ecofy.ms_budgeting.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "idempotency_keys", uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = {"idem_key"}))
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idem_key", nullable = false, length = 200)
    private String key;

    @Column(name = "scope", nullable = false, length = 80)
    private String scope;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
}
