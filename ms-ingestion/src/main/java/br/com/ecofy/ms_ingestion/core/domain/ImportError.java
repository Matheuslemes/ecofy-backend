package br.com.ecofy.ms_ingestion.core.domain;

import br.com.ecofy.ms_ingestion.core.domain.enums.ImportErrorType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ImportError {

    private final UUID id;
    private final UUID importJobId;
    private final Integer lineNumber;
    private final String rawContent;
    private final ImportErrorType errorType;
    private final String errorMessage;
    private final Instant createdAt;

    public ImportError(UUID id,
                       UUID importJobId,
                       Integer lineNumber,
                       String rawContent,
                       ImportErrorType errorType,
                       String errorMessage,
                       Instant createdAt) {

        this.id = Objects.requireNonNull(id);
        this.importJobId = Objects.requireNonNull(importJobId);
        this.lineNumber = lineNumber;
        this.rawContent = rawContent;
        this.errorType = Objects.requireNonNull(errorType);
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static ImportError create(UUID importJobId,
                                     Integer lineNumber,
                                     String rawContent,
                                     ImportErrorType errorType,
                                     String errorMessage) {

        return new ImportError(
                UUID.randomUUID(),
                importJobId,
                lineNumber,
                rawContent,
                errorType,
                errorMessage,
                Instant.now()
        );
    }

    public UUID id() {
        return id;
    }

    public UUID importJobId() {
        return importJobId;
    }

    public Integer lineNumber() {
        return lineNumber;
    }

    public String rawContent() {
        return rawContent;
    }

    public ImportErrorType errorType() {
        return errorType;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Instant createdAt() {
        return createdAt;
    }

}
