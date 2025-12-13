package br.com.ecofy.ms_ingestion.core.application.service;

import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.port.in.IngestTransactionEventUseCase;
import br.com.ecofy.ms_ingestion.core.port.out.PublishTransactionForCategorizationPort;
import br.com.ecofy.ms_ingestion.core.port.out.SaveRawTransactionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TransactionEventIngestionService implements IngestTransactionEventUseCase {

    private final SaveRawTransactionPort saveRawTransactionPort;
    private final PublishTransactionForCategorizationPort publishTransactionForCategorizationPort;

    public TransactionEventIngestionService(SaveRawTransactionPort saveRawTransactionPort,
                                            PublishTransactionForCategorizationPort publishTransactionForCategorizationPort) {
        this.saveRawTransactionPort = Objects.requireNonNull(saveRawTransactionPort, "saveRawTransactionPort must not be null");
        this.publishTransactionForCategorizationPort =
                Objects.requireNonNull(publishTransactionForCategorizationPort, "publishTransactionForCategorizationPort must not be null");
    }

    @Override
    public void ingest(IngestEventCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        List<RawTransaction> transactions = Objects.requireNonNull(
                command.transactions(),
                "transactions must not be null"
        );

        log.info(
                "[TransactionEventIngestionService] - [ingest] -> Ingerindo evento sourceSystem={} payloadId={} totalTx={}",
                command.sourceSystem(), command.payloadId(), transactions.size()
        );

        if (transactions.isEmpty()) {
            log.warn("[TransactionEventIngestionService] - [ingest] -> Nenhuma transação no payload");
            return;
        }

        saveRawTransactionPort.saveAll(transactions);
        publishTransactionForCategorizationPort.publish(transactions);
    }
}