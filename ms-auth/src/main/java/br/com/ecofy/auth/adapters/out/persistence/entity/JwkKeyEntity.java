package br.com.ecofy.auth.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "auth_jwk_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwkKeyEntity {

    @Id
    @Column(name = "key_id", length = 64)
    private String keyId;

    @Column(name = "public_key_pem", nullable = false)
    private String publicKeyPem;

    @Column(name = "algorithm", nullable = false, length = 16)
    private String algorithm;

    @Column(name = "use", nullable = false, length = 8)
    private String use;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
