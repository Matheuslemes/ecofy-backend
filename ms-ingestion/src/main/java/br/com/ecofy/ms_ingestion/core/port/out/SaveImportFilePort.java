package br.com.ecofy.ms_ingestion.core.port.out;


import br.com.ecofy.ms_ingestion.core.domain.ImportFile;

public interface SaveImportFilePort {

    ImportFile save(ImportFile file);

}