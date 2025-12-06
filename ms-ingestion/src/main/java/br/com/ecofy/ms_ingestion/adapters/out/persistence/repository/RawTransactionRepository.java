package br.com.ecofy.ms_ingestion.adapters.out.persistence.repository;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.RawTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RawTransactionRepository extends JpaRepository<RawTransactionEntity, UUID> {
}