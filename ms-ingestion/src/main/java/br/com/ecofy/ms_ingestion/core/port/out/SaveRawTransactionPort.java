package br.com.ecofy.ms_ingestion.core.port.out;

import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;

import java.util.List;

public interface SaveRawTransactionPort {

    void saveAll(List<RawTransaction> transactions);

}