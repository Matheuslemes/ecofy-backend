package br.com.ecofy.ms_ingestion.core.application.exception;

import java.util.UUID;

public class ImportFileNotFoundException extends IngestionException {

    public ImportFileNotFoundException(UUID importFileId) {
        super(IngestionErrorCode.IMPORT_FILE_NOT_FOUND, "ImportFile not found", "importFileId=" + importFileId);
    }

}
