package br.com.ecofy.ms_ingestion.core.port.out;

import br.com.ecofy.ms_ingestion.core.domain.ImportFile;

public interface StoreFilePort {

    // Persiste o arquivo em storage e devolve o caminho salvo (path).
    String store(ImportFile file, byte[] content);

}