package br.com.ecofy.ms_ingestion.core.port.out;

import br.com.ecofy.ms_ingestion.core.domain.ImportJob;

public interface SaveImportJobPort {

    ImportJob save(ImportJob job);

}
