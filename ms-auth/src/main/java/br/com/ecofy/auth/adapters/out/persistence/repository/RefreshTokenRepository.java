package br.com.ecofy.auth.adapters.out.persistence.repository;

import br.com.ecofy.auth.adapters.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue);

}
