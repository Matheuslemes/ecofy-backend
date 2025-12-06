package br.com.ecofy.ms_ingestion.core.port.out;


import br.com.ecofy.ms_ingestion.core.domain.event.ImportJobStatusChangedEvent;
import br.com.ecofy.ms_ingestion.core.domain.event.TransactionsImportedEvent;

public interface PublishIngestionEventPort {

    void publish(TransactionsImportedEvent event);

    void publish(ImportJobStatusChangedEvent event);

}
