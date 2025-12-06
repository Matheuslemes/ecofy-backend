package br.com.ecofy.ms_ingestion.core.port.in;


import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;

import java.util.List;

public interface IngestTransactionEventUseCase {

    record IngestEventCommand(
            String sourceSystem,
            String payloadId,
            List<RawTransaction> transactions
    ) { }

    void ingest(IngestEventCommand command);

}