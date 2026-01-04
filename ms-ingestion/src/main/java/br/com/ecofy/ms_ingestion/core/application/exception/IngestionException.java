package br.com.ecofy.ms_ingestion.core.application.exception;

public class IngestionException extends RuntimeException {

    private final IngestionErrorCode errorCode;
    private final String detail;

    public IngestionException(IngestionErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public IngestionException(IngestionErrorCode errorCode, String message, String detail) {
        super(message);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public IngestionException(IngestionErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public IngestionErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

}
