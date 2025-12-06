package br.com.ecofy.ms_ingestion.core.application.service;

import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.port.in.IngestTransactionEventUseCase;
import br.com.ecofy.ms_ingestion.core.port.out.PublishTransactionForCategorizationPort;
import br.com.ecofy.ms_ingestion.core.port.out.SaveRawTransactionPort;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
public class TransactionEventIngestionService implements IngestTransactionEventUseCase {

    private final SaveRawTransactionPort saveRawTransactionPort;
    private final PublishTransactionForCategorizationPort publishTransactionForCategorizationPort;

    public TransactionEventIngestionService(SaveRawTransactionPort saveRawTransactionPort,
                                            PublishTransactionForCategorizationPort publishTransactionForCategorizationPort) {
        this.saveRawTransactionPort = Objects.requireNonNull(saveRawTransactionPort);
        this.publishTransactionForCategorizationPort = Objects.requireNonNull(publishTransactionForCategorizationPort);
    }

    @Override
    public void ingest(IngestEventCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        List<RawTransaction> transactions = command.transactions();

        log.info("[TransactionEventIngestionService] - [ingest] -> Ingerindo evento sourceSystem={} payloadId={} totalTx={}",
                command.sourceSystem(), command.payloadId(), transactions.size());

        if (transactions.isEmpty()) {
            log.warn("[TransactionEventIngestionService] - [ingest] -> Nenhuma transação no payload");
            return;
        }

        saveRawTransactionPort.saveAll(transactions);
        publishTransactionForCategorizationPort.publish(transactions);
    }
}