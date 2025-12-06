package br.com.ecofy.ms_ingestion.adapters.out.persistence.entity;

import br.com.ecofy.ms_ingestion.core.domain.enums.TransactionSourceType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "raw_transaction")
public class RawTransactionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id", nullable = false)
    private ImportJobEntity importJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_file_id", nullable = false)
    private ImportFileEntity importFile;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 50, nullable = false)
    private TransactionSourceType sourceType;

    @Column(name = "external_id", length = 200)
    private String externalId;

    @Lob
    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RawTransactionEntity() {
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

    public ImportFileEntity getImportFile() {
        return importFile;
    }

    public void setImportFile(ImportFileEntity importFile) {
        this.importFile = importFile;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(TransactionSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
