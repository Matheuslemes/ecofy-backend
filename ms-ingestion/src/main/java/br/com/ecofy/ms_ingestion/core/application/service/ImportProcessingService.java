package br.com.ecofy.ms_ingestion.core.application.service;

import br.com.ecofy.ms_ingestion.config.IngestionProperties;
import br.com.ecofy.ms_ingestion.core.domain.ImportError;
import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.domain.enums.ImportJobStatus;
import br.com.ecofy.ms_ingestion.core.domain.event.ImportJobStatusChangedEvent;
import br.com.ecofy.ms_ingestion.core.domain.event.TransactionsImportedEvent;
import br.com.ecofy.ms_ingestion.core.port.in.RetryFailedImportsUseCase;
import br.com.ecofy.ms_ingestion.core.port.in.StartImportJobUseCase;
import br.com.ecofy.ms_ingestion.core.port.out.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class ImportProcessingService implements StartImportJobUseCase, RetryFailedImportsUseCase {

    private final SaveImportJobPort saveImportJobPort;
    private final LoadImportJobPort loadImportJobPort;
    private final SaveRawTransactionPort saveRawTransactionPort;
    private final SaveImportErrorPort saveImportErrorPort;
    private final SaveImportFilePort saveImportFilePort;
    private final FileContentLoaderPort fileContentLoaderPort;
    private final ParseCsvPort parseCsvPort;
    private final ParseOfxPort parseOfxPort;
    private final PublishTransactionForCategorizationPort publishTransactionForCategorizationPort;
    private final PublishIngestionEventPort publishIngestionEventPort;
    private final IngestionProperties ingestionProperties;

    public ImportProcessingService(SaveImportJobPort saveImportJobPort,
                                   LoadImportJobPort loadImportJobPort,
                                   SaveRawTransactionPort saveRawTransactionPort,
                                   SaveImportErrorPort saveImportErrorPort,
                                   SaveImportFilePort saveImportFilePort,
                                   FileContentLoaderPort fileContentLoaderPort,
                                   ParseCsvPort parseCsvPort,
                                   ParseOfxPort parseOfxPort,
                                   PublishTransactionForCategorizationPort publishTransactionForCategorizationPort,
                                   PublishIngestionEventPort publishIngestionEventPort,
                                   IngestionProperties ingestionProperties) {

        this.saveImportJobPort = Objects.requireNonNull(saveImportJobPort, "saveImportJobPort must not be null");
        this.loadImportJobPort = Objects.requireNonNull(loadImportJobPort, "loadImportJobPort must not be null");
        this.saveRawTransactionPort = Objects.requireNonNull(saveRawTransactionPort, "saveRawTransactionPort must not be null");
        this.saveImportErrorPort = Objects.requireNonNull(saveImportErrorPort, "saveImportErrorPort must not be null");
        this.saveImportFilePort = Objects.requireNonNull(saveImportFilePort, "saveImportFilePort must not be null");
        this.fileContentLoaderPort = Objects.requireNonNull(fileContentLoaderPort, "fileContentLoaderPort must not be null");
        this.parseCsvPort = Objects.requireNonNull(parseCsvPort, "parseCsvPort must not be null");
        this.parseOfxPort = Objects.requireNonNull(parseOfxPort, "parseOfxPort must not be null");
        this.publishTransactionForCategorizationPort =
                Objects.requireNonNull(publishTransactionForCategorizationPort, "publishTransactionForCategorizationPort must not be null");
        this.publishIngestionEventPort =
                Objects.requireNonNull(publishIngestionEventPort, "publishIngestionEventPort must not be null");
        this.ingestionProperties = Objects.requireNonNull(ingestionProperties, "ingestionProperties must not be null");
    }

    // INÍCIO DO JOB
    @Override
    public ImportJob start(StartImportJobCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        UUID importFileId = command.importFileId();

        log.info("[ImportProcessingService] - [start] -> Iniciando job para importFileId={}", importFileId);

        ImportJob job = ImportJob.create(importFileId);
        job = saveImportJobPort.save(job);

        processJob(job);

        return job;
    }

    // PROCESSAMENTO PRINCIPAL DO JOB
    private void processJob(ImportJob job) {
        Objects.requireNonNull(job, "job must not be null");

        ImportJobStatus previousStatus = job.status();
        job.markRunning();
        saveImportJobPort.save(job);

        publishIngestionEventPort.publish(
                new ImportJobStatusChangedEvent(
                        job.id(),
                        previousStatus,
                        job.status(),
                        job.updatedAt()
                )
        );

        List<RawTransaction> allTransactions = new ArrayList<>();
        List<ImportError> allErrors = new ArrayList<>();

        try {
            log.info("[ImportProcessingService] - [processJob] -> Processando job id={}", job.id());

            // 1) Carrega o conteúdo do arquivo via storage
            byte[] fileBytes = fileContentLoaderPort.load(String.valueOf(job.importFileId()));
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);

            // 2) Tenta parsear como CSV; se falhar, tenta OFX
            List<RawTransaction> parsedTransactions;
            List<ImportError> parsedErrors = new ArrayList<>(); // hook p/ validações futuras

            try {
                parsedTransactions = parseCsvPort.parse(job, fileContent);
            } catch (Exception csvEx) {
                log.debug(
                        "[ImportProcessingService] - [processJob] -> Falha ao parsear como CSV, tentando OFX jobId={} error={}",
                        job.id(), csvEx.getMessage(), csvEx
                );
                parsedTransactions = parseOfxPort.parse(job, fileContent);
            }

            allTransactions.addAll(parsedTransactions);
            allErrors.addAll(parsedErrors);

            // 3) Persistir transações e publicar para categorização
            if (!allTransactions.isEmpty()) {
                saveRawTransactionPort.saveAll(allTransactions);

                publishTransactionForCategorizationPort.publish(allTransactions);

                publishIngestionEventPort.publish(
                        new TransactionsImportedEvent(
                                job.id(),
                                allTransactions.stream().map(RawTransaction::id).toList()
                        )
                );
            }

            // 4) Persistir erros, se existirem
            if (!allErrors.isEmpty()) {
                saveImportErrorPort.saveAll(allErrors);
            }

            // 5) Atualizar status final do job com base em erros
            if (allErrors.isEmpty()) {
                job.markCompleted();
            } else if (allErrors.size() > ingestionProperties.getMaxErrorsPerJob()) {
                job.markFailed();
            } else {
                job.markCompletedWithErrors();
            }

            saveImportJobPort.save(job);

            publishIngestionEventPort.publish(
                    new ImportJobStatusChangedEvent(
                            job.id(),
                            ImportJobStatus.RUNNING,
                            job.status(),
                            job.updatedAt()
                    )
            );

        } catch (Exception e) {
            log.error(
                    "[ImportProcessingService] - [processJob] -> Erro ao processar job id={} error={}",
                    job.id(), e.getMessage(), e
            );

            job.markFailed();
            saveImportJobPort.save(job);

            publishIngestionEventPort.publish(
                    new ImportJobStatusChangedEvent(
                            job.id(),
                            ImportJobStatus.RUNNING,
                            job.status(),
                            job.updatedAt()
                    )
            );
        }
    }

    // RETRY DE JOBS COM FALHA / ERROS
    @Override
    public void retry(RetryFailedImportsCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        int maxJobs = command.maxJobs();
        log.info("[ImportProcessingService] - [retry] -> Disparo de retry solicitado maxJobs={}", maxJobs);

        // Requer que LoadImportJobPort tenha o método loadJobsToRetry(int maxJobs)
        List<ImportJob> jobsToRetry = loadImportJobPort.loadJobsToRetry(maxJobs);

        if (jobsToRetry.isEmpty()) {
            log.info("[ImportProcessingService] - [retry] -> Nenhum job elegível para retry encontrado");
            return;
        }

        log.info("[ImportProcessingService] - [retry] -> Encontrados {} jobs para retry", jobsToRetry.size());

        for (ImportJob job : jobsToRetry) {
            ImportJobStatus status = job.status();
            if (status != ImportJobStatus.FAILED &&
                    status != ImportJobStatus.COMPLETED_WITH_ERRORS) {

                log.debug(
                        "[ImportProcessingService] - [retry] -> Ignorando job id={} com status={}",
                        job.id(), status
                );
                continue;
            }

            log.info(
                    "[ImportProcessingService] - [retry] -> Reprocessando job id={} statusAtual={}",
                    job.id(), status
            );
            processJob(job);
        }
    }
}
