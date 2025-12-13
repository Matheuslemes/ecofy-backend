package br.com.ecofy.ms_ingestion.core.port.out;


import br.com.ecofy.ms_ingestion.core.domain.ImportFile;

import java.util.UUID;

public interface SaveImportFilePort {

    ImportFile save(ImportFile file);

    ImportFile getById(UUID id);

}