package br.com.ecofy.auth.adapters.out.persistence.repository;

import br.com.ecofy.auth.adapters.out.persistence.entity.JwkKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JwkKeyRepository extends JpaRepository<JwkKeyEntity, String> {

    List<JwkKeyEntity> findByActiveTrue();

}
