package br.com.ecofy.ms_ingestion.core.port.out;


import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;

import java.util.List;

public interface ParseCsvPort {

    List<RawTransaction> parse(ImportJob job, String csvContent);

}