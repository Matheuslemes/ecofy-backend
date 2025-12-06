package br.com.ecofy.ms_ingestion.core.port.out;

import br.com.ecofy.ms_ingestion.core.domain.ImportError;

import java.util.List;

public interface SaveImportErrorPort {

    void saveAll(List<ImportError> errors);
}