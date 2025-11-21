package br.com.ecofy.auth.adapters.out.persistence.repository;

import br.com.ecofy.auth.adapters.out.persistence.entity.ClientApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientApplicationRepository extends JpaRepository<ClientApplicationEntity, String> {

    Optional<ClientApplicationEntity> findByClientId(String clientId);

}
