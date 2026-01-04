package br.com.ecofy.ms_ingestion.core.application.exception;

import java.util.UUID;

public class ImportJobNotFoundException extends IngestionException {

    public ImportJobNotFoundException(UUID jobId) {
        super(IngestionErrorCode.IMPORT_JOB_NOT_FOUND, "ImportJob not found", "jobId=" + jobId);
    }

}
