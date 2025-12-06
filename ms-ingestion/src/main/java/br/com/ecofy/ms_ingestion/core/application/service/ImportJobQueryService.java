package br.com.ecofy.ms_ingestion.core.application.service;

import br.com.ecofy.ms_ingestion.core.domain.ImportError;
import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.port.in.GetImportJobStatusUseCase;
import br.com.ecofy.ms_ingestion.core.port.out.LoadImportJobPort;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class ImportJobQueryService implements GetImportJobStatusUseCase {

    private final LoadImportJobPort loadImportJobPort;
    // Em uma implementação completa, teríamos LoadImportErrorPort
    // Aqui mantemos simples (lista vazia) para manter o contrato.

    public ImportJobQueryService(LoadImportJobPort loadImportJobPort) {
        this.loadImportJobPort = Objects.requireNonNull(loadImportJobPort);
    }

    @Override
    public GetImportJobStatusUseCase.ImportJobStatusView getById(UUID jobId) {
        log.debug("[ImportJobQueryService] - [getById] -> Buscando status de job jobId={}", jobId);

        ImportJob job = loadImportJobPort.loadById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("ImportJob not found"));

        List<ImportError> errors = Collections.emptyList(); // placeholder

        return new ImportJobStatusView(job, errors);
    }

}
