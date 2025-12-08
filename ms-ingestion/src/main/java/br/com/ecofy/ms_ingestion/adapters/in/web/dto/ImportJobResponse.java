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
        int processedRecords,
        int successCount,
        int errorCount,
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
                job.processedRecords(),
                job.successCount(),
                job.errorCount(),
                job.startedAt(),
                job.finishedAt(),
                job.createdAt(),
                job.updatedAt()
        );
    }
}
