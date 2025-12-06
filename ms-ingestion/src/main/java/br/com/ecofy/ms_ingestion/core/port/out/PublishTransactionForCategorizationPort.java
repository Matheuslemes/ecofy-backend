package br.com.ecofy.ms_ingestion.core.port.out;

import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;

import java.util.List;

public interface PublishTransactionForCategorizationPort {

    void publish(List<RawTransaction> transactions);

}