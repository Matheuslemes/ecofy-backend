package br.com.ecofy.ms_ingestion.core.application.exception;

import org.springframework.http.HttpStatus;

public enum IngestionErrorCode {

    INVALID_COMMAND(HttpStatus.BAD_REQUEST, "INVALID_COMMAND"),
    INVALID_FILE_SIZE(HttpStatus.BAD_REQUEST, "INVALID_FILE_SIZE"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE"),
    IMPORT_FILE_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "IMPORT_FILE_TYPE_REQUIRED"),

    IMPORT_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "IMPORT_JOB_NOT_FOUND"),
    IMPORT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMPORT_FILE_NOT_FOUND"),
    IMPORT_FILE_STORED_PATH_MISSING(HttpStatus.UNPROCESSABLE_ENTITY, "IMPORT_FILE_STORED_PATH_MISSING"),
    UNSUPPORTED_IMPORT_FILE_TYPE(HttpStatus.BAD_REQUEST, "UNSUPPORTED_IMPORT_FILE_TYPE"),

    STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR"),
    PARSE_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "PARSE_ERROR"),
    PERSISTENCE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PERSISTENCE_ERROR"),
    PUBLISH_ERROR(HttpStatus.BAD_GATEWAY, "PUBLISH_ERROR"),

    EMPTY_TRANSACTIONS_PAYLOAD(HttpStatus.BAD_REQUEST, "EMPTY_TRANSACTIONS_PAYLOAD");

    private final HttpStatus httpStatus;
    private final String code;

    IngestionErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

}
