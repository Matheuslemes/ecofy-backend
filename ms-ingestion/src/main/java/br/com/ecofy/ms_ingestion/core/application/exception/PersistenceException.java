package br.com.ecofy.ms_ingestion.core.application.exception;

public class PersistenceException extends IngestionException {

    public PersistenceException(String message, Throwable cause) {
        super(IngestionErrorCode.PERSISTENCE_ERROR, message, cause);
    }

}
