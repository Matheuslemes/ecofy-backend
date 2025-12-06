package br.com.ecofy.ms_ingestion.adapters.out.persistence.entity;

import br.com.ecofy.ms_ingestion.core.domain.enums.ImportErrorType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "import_error")
public class ImportErrorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id", nullable = false)
    private ImportJobEntity importJob;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Lob
    @Column(name = "raw_line")
    private String rawLine;

    @Column(name = "message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", length = 50, nullable = false)
    private ImportErrorType errorType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ImportErrorEntity() {
    }

    // getters e setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ImportJobEntity getImportJob() {
        return importJob;
    }

    public void setImportJob(ImportJobEntity importJob) {
        this.importJob = importJob;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ImportErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ImportErrorType errorType) {
        this.errorType = errorType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
