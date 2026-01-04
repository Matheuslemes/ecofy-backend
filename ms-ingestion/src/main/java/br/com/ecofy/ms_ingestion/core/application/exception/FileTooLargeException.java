package br.com.ecofy.ms_ingestion.core.application.exception;

public class FileTooLargeException extends IngestionException {

    public FileTooLargeException(long sizeBytes, long maxBytes) {
        super(IngestionErrorCode.FILE_TOO_LARGE, "File too large", "sizeBytes=" + sizeBytes + ", maxBytes=" + maxBytes);
    }

}
