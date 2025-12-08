package br.com.ecofy.ms_ingestion.adapters.out.persistence;

import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportErrorEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportFileEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.ImportJobEntity;
import br.com.ecofy.ms_ingestion.adapters.out.persistence.entity.RawTransactionEntity;
import br.com.ecofy.ms_ingestion.core.domain.ImportError;
import br.com.ecofy.ms_ingestion.core.domain.ImportFile;
import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportJobStatus;
import br.com.ecofy.ms_ingestion.core.domain.valueobject.Money;
import br.com.ecofy.ms_ingestion.core.domain.valueobject.TransactionDate;

import java.time.Instant;
import java.util.Objects;

final class PersistenceMapper {

    private PersistenceMapper() {
    }

    // RAW TRANSACTION
    static RawTransactionEntity toEntity(RawTransaction tx,
                                         ImportJobEntity jobEntity,
                                         ImportFileEntity fileEntity) {

        Objects.requireNonNull(tx, "tx must not be null");
        Objects.requireNonNull(jobEntity, "jobEntity must not be null");
        Objects.requireNonNull(fileEntity, "fileEntity must not be null");

        RawTransactionEntity e = new RawTransactionEntity();
        e.setId(tx.id());
        e.setImportJob(jobEntity);
        e.setImportFile(fileEntity);

        // domínio: TransactionDate date()
        e.setTransactionDate(tx.date().value());

        e.setDescription(tx.description());

        // domínio: Money amount()
        e.setAmount(tx.amount().amount());
        e.setCurrency(tx.amount().currency());

        e.setSourceType(tx.sourceType());
        e.setExternalId(tx.externalId());

        // domínio atual não expõe rawPayload → persiste como null por enquanto
        e.setRawPayload(null);

        e.setCreatedAt(tx.createdAt());
        return e;
    }

    static RawTransaction toDomain(RawTransactionEntity e) {
        Objects.requireNonNull(e, "entity must not be null");
        Objects.requireNonNull(e.getImportJob(), "importJob must not be null");

        Money money = new Money(e.getAmount(), e.getCurrency());
        TransactionDate date = new TransactionDate(e.getTransactionDate());

        return new RawTransaction(
                e.getId(),
                e.getImportJob().getId(),
                e.getExternalId(),
                e.getDescription(),
                date,
                money,
                e.getSourceType(),
                e.getCreatedAt()
        );
    }

    // IMPORT FILE
    static ImportFileEntity toEntity(ImportFile file) {
        Objects.requireNonNull(file, "file must not be null");

        ImportFileEntity e = new ImportFileEntity();
        e.setId(file.id());

        // domínio: originalFileName() / storedPath() / type()
        e.setOriginalFilename(file.originalFileName());
        e.setStoredFilename(file.storedPath());
        e.setFileType(file.type());

        // domínio ainda não modela sourceType/contentType → mantém null
        e.setSourceType(null);
        e.setContentType(null);

        e.setSizeBytes(file.sizeBytes());
        e.setUploadedAt(file.uploadedAt());

        // domínio não tem createdAt -> usa o próprio uploadedAt como base
        Instant created = file.uploadedAt() != null ? file.uploadedAt() : Instant.now();
        e.setCreatedAt(created);

        return e;
    }

    static ImportFile toDomain(ImportFileEntity e) {
        Objects.requireNonNull(e, "entity must not be null");

        // mapeia só o que o domínio conhece hoje
        return new ImportFile(
                e.getId(),
                e.getOriginalFilename(),
                e.getStoredFilename(),
                e.getFileType(),
                e.getSizeBytes(),
                e.getUploadedAt()
        );
    }

    // IMPORT JOB
    static ImportJobEntity toEntity(ImportJob job, ImportFileEntity fileEntity) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(fileEntity, "fileEntity must not be null");

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
        Objects.requireNonNull(e, "entity must not be null");
        ImportFileEntity file = Objects.requireNonNull(e.getImportFile(), "importFile must not be null");

        ImportJobStatus status = e.getStatus();

        return new ImportJob(
                e.getId(),
                file.getId(),
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

    // IMPORT ERROR
    static ImportErrorEntity toEntity(ImportError error, ImportJobEntity jobEntity) {
        Objects.requireNonNull(error, "error must not be null");
        Objects.requireNonNull(jobEntity, "jobEntity must not be null");

        ImportErrorEntity e = new ImportErrorEntity();
        e.setId(error.id());
        e.setImportJob(jobEntity);
        e.setLineNumber(error.lineNumber());

        // domínio: rawContent() / errorMessage()
        e.setRawLine(error.rawContent());
        e.setMessage(error.errorMessage());

        e.setErrorType(error.errorType());
        e.setCreatedAt(error.createdAt());
        return e;
    }

    static ImportError toDomain(ImportErrorEntity e) {
        Objects.requireNonNull(e, "entity must not be null");
        Objects.requireNonNull(e.getImportJob(), "importJob must not be null");

        return new ImportError(
                e.getId(),
                e.getImportJob().getId(),
                e.getLineNumber(),
                e.getRawLine(),
                e.getErrorType(),
                e.getMessage(),
                e.getCreatedAt()
        );
    }
}
