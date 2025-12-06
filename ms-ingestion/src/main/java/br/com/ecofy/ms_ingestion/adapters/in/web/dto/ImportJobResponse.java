package br.com.ecofy.ms_ingestion.adapters.in.web.dto;

import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportJobStatus;

import java.time.Instant;
import java.util.UUID;

public record ImportJobResponse(
        UUID id,
        UUID importFileId,
        ImportJobStatus status,
        int totalRecords,
        int successRecords,
        int errorRecords,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ImportJobResponse fromDomain(ImportJob job) {
        return new ImportJobResponse(
                job.id(),
                job.importFileId(),
                job.status(),
                job.totalRecords(),
                job.successRecords(),
                job.errorRecords(),
                job.startedAt(),
                job.finishedAt(),
                job.createdAt(),
                job.updatedAt()
        );
    }
}