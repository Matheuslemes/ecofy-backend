package br.com.ecofy.ms_ingestion.core.port.out;

import br.com.ecofy.ms_ingestion.core.domain.ImportJob;

import java.util.Optional;
import java.util.UUID;

public interface LoadImportJobPort {

    Optional<ImportJob> loadById(UUID id);

}