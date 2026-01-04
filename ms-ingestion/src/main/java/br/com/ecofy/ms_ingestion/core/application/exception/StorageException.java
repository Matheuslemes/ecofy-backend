package br.com.ecofy.ms_ingestion.core.application.exception;

public class StorageException extends IngestionException {

    public StorageException(String message, Throwable cause) {
        super(IngestionErrorCode.STORAGE_ERROR, message, cause);
    }

}
