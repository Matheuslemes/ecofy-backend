package br.com.ecofy.ms_ingestion.adapters.in.web.dto;

import br.com.ecofy.ms_ingestion.core.domain.ImportError;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportErrorType;

import java.time.Instant;
import java.util.UUID;

public record ImportErrorResponse(
        UUID id,
        UUID importJobId,
        Integer lineNumber,
        String rawContent,
        ImportErrorType errorType,
        String errorMessage,
        Instant createdAt
) {
    public static ImportErrorResponse fromDomain(ImportError error) {
        return new ImportErrorResponse(
                error.id(),
                error.importJobId(),
                error.lineNumber(),
                error.rawContent(),
                error.errorType(),
                error.errorMessage(),
                error.createdAt()
        );
    }
}