package br.com.ecofy.ms_ingestion.core.domain;

import br.com.ecofy.ms_ingestion.core.domain.enums.ImportJobStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ImportJob {

    private final UUID id;
    private final UUID importFileId;
    private ImportJobStatus status;
    private int totalRecords;
    private int successRecords;
    private int errorRecords;
    private Instant startedAt;
    private Instant finishedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public ImportJob(UUID id,
                     UUID importFileId,
                     ImportJobStatus status,
                     int totalRecords,
                     int successRecords,
                     int errorRecords,
                     Instant startedAt,
                     Instant finishedAt,
                     Instant createdAt,
                     Instant updatedAt) {

        this.id = Objects.requireNonNull(id);
        this.importFileId = Objects.requireNonNull(importFileId);
        this.status = Objects.requireNonNull(status);
        this.totalRecords = totalRecords;
        this.successRecords = successRecords;
        this.errorRecords = errorRecords;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static ImportJob create(UUID importFileId) {
        Instant now = Instant.now();
        return new ImportJob(
                UUID.randomUUID(),
                importFileId,
                ImportJobStatus.PENDING,
                0,
                0,
                0,
                null,
                null,
                now,
                now
        );
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public void markRunning() {
        this.status = ImportJobStatus.RUNNING;
        this.startedAt = Instant.now();
        touch();
    }

    public void incrementSuccess() {
        this.successRecords++;
        this.totalRecords++;
        touch();
    }

    public void incrementError() {
        this.errorRecords++;
        this.totalRecords++;
        touch();
    }

    public void markCompleted() {
        if (errorRecords > 0) {
            this.status = ImportJobStatus.COMPLETED_WITH_ERRORS;
        } else {
            this.status = ImportJobStatus.COMPLETED;
        }
        this.finishedAt = Instant.now();
        touch();
    }

    public void markFailed() {
        this.status = ImportJobStatus.FAILED;
        this.finishedAt = Instant.now();
        touch();
    }

    // getters

    public UUID id() {
        return id;
    }

    public UUID importFileId() {
        return importFileId;
    }

    public ImportJobStatus status() {
        return status;
    }

    public int totalRecords() {
        return totalRecords;
    }

    public int successRecords() {
        return successRecords;
    }

    public int errorRecords() {
        return errorRecords;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}
