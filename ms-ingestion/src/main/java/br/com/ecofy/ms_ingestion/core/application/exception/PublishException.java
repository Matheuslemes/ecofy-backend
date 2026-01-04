package br.com.ecofy.ms_ingestion.core.application.exception;

public class PublishException extends IngestionException {

    public PublishException(String message, Throwable cause) {
        super(IngestionErrorCode.PUBLISH_ERROR, message, cause);
    }

}
