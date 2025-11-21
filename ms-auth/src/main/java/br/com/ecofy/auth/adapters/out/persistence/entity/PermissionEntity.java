package br.com.ecofy.auth.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity {

    @Id
    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "domain", length = 64)
    private String domain;

}
