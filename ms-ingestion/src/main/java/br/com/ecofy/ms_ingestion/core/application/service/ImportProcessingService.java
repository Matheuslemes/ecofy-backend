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
        this.saveImportJobPort = Objects.requireNonNull(saveImportJobPort);
        this.loadImportJobPort = Objects.requireNonNull(loadImportJobPort);
        this.saveRawTransactionPort = Objects.requireNonNull(saveRawTransactionPort);
        this.saveImportErrorPort = Objects.requireNonNull(saveImportErrorPort);
        this.saveImportFilePort = Objects.requireNonNull(saveImportFilePort);
        this.fileContentLoaderPort = Objects.requireNonNull(fileContentLoaderPort);
        this.parseCsvPort = Objects.requireNonNull(parseCsvPort);
        this.parseOfxPort = Objects.requireNonNull(parseOfxPort);
        this.publishTransactionForCategorizationPort = Objects.requireNonNull(publishTransactionForCategorizationPort);
        this.publishIngestionEventPort = Objects.requireNonNull(publishIngestionEventPort);
        this.ingestionProperties = Objects.requireNonNull(ingestionProperties);
    }

    @Override
    public ImportJob start(StartImportJobCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        UUID importFileId = command.importFileId();

        log.info("[ImportProcessingService] - [start] -> Iniciando job para importFileId={}", importFileId);

        // carrega ImportJob ou cria se não existir
        ImportJob job = ImportJob.create(importFileId);
        job = saveImportJobPort.save(job);

        processJob(job);

        return job;
    }

    private void processJob(ImportJob job) {
        ImportJobStatus oldStatus = job.status();
        job.markRunning();
        saveImportJobPort.save(job);

        publishIngestionEventPort.publish(new ImportJobStatusChangedEvent(
                job.id(), oldStatus, job.status(), job.updatedAt()
        ));

        List<RawTransaction> allTransactions = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try {
            // carregar ImportFile (metadados) via SaveImportFilePort → normalmente via adapter
            // aqui simulamos recuperando path pelo ID usando o mesmo port.
            // Em uma implementação real, haveria LoadImportFilePort separado.

            // Padrão: FileEntity -> ImportFile -> path
            // Vamos supor que SaveImportFilePort consegue nos devolver por ID na infra real.
            // Para fins de domínio, seguimos assim.

            // carregar conteúdo
            // Em projeto real, use outro port; aqui só demonstrativo:
            // String path = importFile.storedPath();
            // byte[] bytes = fileContentLoaderPort.load(path);

            // Para simplificar o domínio aqui, assumiremos que adapter fará isso
            // via SaveImportJobPort/LoadImportJobPort + FilePort.

            // LOG
            log.info("[ImportProcessingService] - [processJob] -> Processando job id={}", job.id());

            // parse dummy (adapter real vai decidir CSV/OFX pelo ImportFile)
            // Aqui, você chamaria ParseCsvPort/ParseOfxPort conforme tipo:
            // List<RawTransaction> parsed = parseCsvPort.parse(job, csvContent);

            // Como faltam alguns ports de leitura completa de ImportFile,
            // vamos assumir que o adapter de aplicação chamará processJob(Job) com
            // tudo que precisa já resolvido. Fica o gancho pronto.

            // Ao final, salvar transações + erros
            if (!allTransactions.isEmpty()) {
                saveRawTransactionPort.saveAll(allTransactions);
                publishTransactionForCategorizationPort.publish(allTransactions);
                publishIngestionEventPort.publish(
                        new TransactionsImportedEvent(job.id(),
                                allTransactions.stream().map(RawTransaction::id).toList())
                );
            }

            if (!errors.isEmpty()) {
                saveImportErrorPort.saveAll(errors);
            }

            if (errors.isEmpty()) {
                job.markCompleted();
            } else {
                if (errors.size() > ingestionProperties.getMaxErrorsPerJob()) {
                    job.markFailed();
                } else {
                    job.markCompleted();
                }
            }

            saveImportJobPort.save(job);
            publishIngestionEventPort.publish(
                    new ImportJobStatusChangedEvent(job.id(), ImportJobStatus.RUNNING, job.status(), job.updatedAt())
            );

        } catch (Exception e) {
            log.error("[ImportProcessingService] - [processJob] -> Erro ao processar job id={} error={}",
                    job.id(), e.getMessage(), e);
            job.markFailed();
            saveImportJobPort.save(job);
            publishIngestionEventPort.publish(
                    new ImportJobStatusChangedEvent(job.id(), ImportJobStatus.RUNNING, job.status(), job.updatedAt())
            );
        }
    }

    @Override
    public void retry(RetryFailedImportsUseCase.RetryFailedImportsCommand command) {
        // aqui normalmente buscaria jobs FAIL/PENDING no repositório (precisa de query extra),
        // mas como ainda não definimos, deixo a assinatura pronta.
        log.info("[ImportProcessingService] - [retry] -> Disparo de retry solicitado maxJobs={}",
                command.maxJobs());
        // Implementação real dependerá de queries em ImportJobRepository.
    }
}