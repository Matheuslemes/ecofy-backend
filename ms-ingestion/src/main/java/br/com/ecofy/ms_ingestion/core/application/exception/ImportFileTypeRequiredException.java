package br.com.ecofy.ms_ingestion.core.application.exception;

public class ImportFileTypeRequiredException extends IngestionException {

    public ImportFileTypeRequiredException() {
        super(IngestionErrorCode.IMPORT_FILE_TYPE_REQUIRED, "ImportFileType (type) must not be null for uploaded file");
    }

}
