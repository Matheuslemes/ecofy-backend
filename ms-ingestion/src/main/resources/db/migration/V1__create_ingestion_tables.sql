-- V1__create_ingestion_tables.sql
-- Schema base do ms-ingestion (import_file, import_job, raw_transaction, import_error)

-- Tabela: import_file
CREATE TABLE import_file (
    id                UUID           PRIMARY KEY,
    original_filename VARCHAR(255)   NOT NULL,
    stored_filename   VARCHAR(255)   NOT NULL,
    file_type         VARCHAR(50)    NOT NULL,  -- ImportFileType (CSV, OFX)
    source_type       VARCHAR(50)    NOT NULL,  -- TransactionSourceType (FILE_CSV, FILE_OFX, ...)
    content_type      VARCHAR(100),
    size_bytes        BIGINT         NOT NULL,
    uploaded_at       TIMESTAMPTZ    NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_import_file_type
    ON import_file (file_type);

CREATE INDEX idx_import_file_source_type
    ON import_file (source_type);

CREATE INDEX idx_import_file_created_at
    ON import_file (created_at);


-- Tabela: import_job
CREATE TABLE import_job (
    id                UUID           PRIMARY KEY,
    import_file_id    UUID           NOT NULL,
    status            VARCHAR(50)    NOT NULL,  -- ImportJobStatus (PENDING, RUNNING, ...)
    total_records     INTEGER,
    processed_records INTEGER,
    success_count     INTEGER,
    error_count       INTEGER,
    started_at        TIMESTAMPTZ,
    finished_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ,

    CONSTRAINT fk_import_job_file
        FOREIGN KEY (import_file_id)
            REFERENCES import_file (id)
);

CREATE INDEX idx_import_job_file_id
    ON import_job (import_file_id);

CREATE INDEX idx_import_job_status
    ON import_job (status);

CREATE INDEX idx_import_job_created_at
    ON import_job (created_at);

-- Tabela: raw_transaction
CREATE TABLE raw_transaction (
    id               UUID           PRIMARY KEY,
    import_job_id    UUID           NOT NULL,
    import_file_id   UUID           NOT NULL,
    transaction_date DATE           NOT NULL,
    description      VARCHAR(500),
    amount           NUMERIC(19,4)  NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    source_type      VARCHAR(50)    NOT NULL,   -- TransactionSourceType
    external_id      VARCHAR(200),
    raw_payload      TEXT,
    created_at       TIMESTAMPTZ    NOT NULL,

    CONSTRAINT fk_raw_tx_job
        FOREIGN KEY (import_job_id)
            REFERENCES import_job (id),

    CONSTRAINT fk_raw_tx_file
        FOREIGN KEY (import_file_id)
            REFERENCES import_file (id)
);

CREATE INDEX idx_raw_tx_job_id
    ON raw_transaction (import_job_id);

CREATE INDEX idx_raw_tx_file_id
    ON raw_transaction (import_file_id);

CREATE INDEX idx_raw_tx_date
    ON raw_transaction (transaction_date);

CREATE INDEX idx_raw_tx_source_type
    ON raw_transaction (source_type);

-- Tabela: import_error
CREATE TABLE import_error (
    id           UUID           PRIMARY KEY,
    import_job_id UUID          NOT NULL,
    line_number  INTEGER,
    raw_line     TEXT,
    message      VARCHAR(1000),
    error_type   VARCHAR(50)    NOT NULL,  -- ImportErrorType (PARSE_ERROR, VALIDATION_ERROR, ...)
    created_at   TIMESTAMPTZ    NOT NULL,

    CONSTRAINT fk_import_error_job
        FOREIGN KEY (import_job_id)
            REFERENCES import_job (id)
);

CREATE INDEX idx_import_error_job_id
    ON import_error (import_job_id);

CREATE INDEX idx_import_error_type
    ON import_error (error_type);

CREATE INDEX idx_import_error_created_at
    ON import_error (created_at);
