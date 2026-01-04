package br.com.ecofy.ms_ingestion.core.application.exception;

import java.util.UUID;

public class ImportFileStoredPathMissingException extends IngestionException {

    public ImportFileStoredPathMissingException(UUID importFileId) {
        super(
                IngestionErrorCode.IMPORT_FILE_STORED_PATH_MISSING,
                "ImportFile storedPath is null/blank",
                "importFileId=" + importFileId
        );
    }

}
