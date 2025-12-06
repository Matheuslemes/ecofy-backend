package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.*;
import br.com.ecofy.ms_ingestion.core.domain.*;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportJobStatus;
import br.com.ecofy.ms_ingestion.core.domain.valueobject.Money;
import br.com.ecofy.ms_ingestion.core.domain.valueobject.TransactionDate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

final class PersistenceMapper {

    private PersistenceMapper() {
    }

    // ================= RAW TRANSACTION =================

    static RawTransactionEntity toEntity(RawTransaction tx,
                                         ImportJobEntity jobEntity,
                                         ImportFileEntity fileEntity) {

        RawTransactionEntity e = new RawTransactionEntity();
        e.setId(tx.id());
        e.setImportJob(jobEntity);
        e.setImportFile(fileEntity);
        e.setTransactionDate(tx.transactionDate().value());
        e.setDescription(tx.description());
        e.setAmount(tx.amount().amount());
        e.setCurrency(tx.amount().currency());
        e.setSourceType(tx.sourceType());
        e.setExternalId(tx.externalId());
        e.setRawPayload(tx.rawPayload());
        e.setCreatedAt(tx.createdAt());
        return e;
    }

    static RawTransaction toDomain(RawTransactionEntity e) {
        Money money = new Money(e.getAmount(), e.getCurrency());
        TransactionDate date = new TransactionDate(e.getTransactionDate());

        return new RawTransaction(
                e.getId(),
                e.getImportJob().getId(),
                e.getImportFile().getId(),
                date,
                e.getDescription(),
                money,
                e.getSourceType(),
                e.getExternalId(),
                e.getRawPayload(),
                e.getCreatedAt()
        );
    }

    // ================= IMPORT FILE =================

    static ImportFileEntity toEntity(ImportFile file) {
        ImportFileEntity e = new ImportFileEntity();
        e.setId(file.id());
        e.setOriginalFilename(file.originalFilename());
        e.setStoredFilename(file.storedFilename());
        e.setFileType(file.fileType());
        e.setSourceType(file.sourceType());
        e.setContentType(file.contentType());
        e.setSizeBytes(file.sizeBytes());
        e.setUploadedAt(file.uploadedAt());
        e.setCreatedAt(file.createdAt());
        return e;
    }

    static ImportFile toDomain(ImportFileEntity e) {
        return new ImportFile(
                e.getId(),
                e.getOriginalFilename(),
                e.getStoredFilename(),
                e.getFileType(),
                e.getSourceType(),
                e.getContentType(),
                e.getSizeBytes(),
                e.getUploadedAt(),
                e.getCreatedAt()
        );
    }

    // ================= IMPORT JOB =================

    static ImportJobEntity toEntity(ImportJob job, ImportFileEntity fileEntity) {
        ImportJobEntity e = new ImportJobEntity();
        e.setId(job.id());
        e.setImportFile(fileEntity);
        e.setStatus(job.status());
        e.setTotalRecords(job.totalRecords());
        e.setProcessedRecords(job.processedRecords());
        e.setSuccessCount(job.successCount());
        e.setErrorCount(job.errorCount());
        e.setStartedAt(job.startedAt());
        e.setFinishedAt(job.finishedAt());
        e.setCreatedAt(job.createdAt());
        e.setUpdatedAt(job.updatedAt());
        return e;
    }

    static ImportJob toDomain(ImportJobEntity e) {
        UUID fileId = Objects.requireNonNull(e.getImportFile()).getId();
        ImportJobStatus status = e.getStatus();

        return new ImportJob(
                e.getId(),
                fileId,
                status,
                e.getTotalRecords(),
                e.getProcessedRecords(),
                e.getSuccessCount(),
                e.getErrorCount(),
                e.getStartedAt(),
                e.getFinishedAt(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    // ================= IMPORT ERROR =================

    static ImportErrorEntity toEntity(ImportError error, ImportJobEntity jobEntity) {
        ImportErrorEntity e = new ImportErrorEntity();
        e.setId(error.id());
        e.setImportJob(jobEntity);
        e.setLineNumber(error.lineNumber());
        e.setRawLine(error.rawLine());
        e.setMessage(error.message());
        e.setErrorType(error.errorType());
        e.setCreatedAt(error.createdAt());
        return e;
    }

    static ImportError toDomain(ImportErrorEntity e) {
        return new ImportError(
                e.getId(),
                e.getImportJob().getId(),
                e.getLineNumber(),
                e.getRawLine(),
                e.getMessage(),
                e.getErrorType(),
                e.getCreatedAt()
        );
    }
}
