package br.com.ecofy.auth.adapters.out.persistence.repository;

import br.com.ecofy.auth.adapters.out.persistence.entity.AuthUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID> {

    Optional<AuthUserEntity> findByEmailIgnoreCase(String email);

}
