package br.com.ecofy.auth.adapters.out.persistence.repository;

import br.com.ecofy.auth.adapters.out.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, String> {

}
