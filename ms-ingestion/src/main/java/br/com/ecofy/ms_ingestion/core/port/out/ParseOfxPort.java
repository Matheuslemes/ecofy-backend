package br.com.ecofy.ms_ingestion.core.port.out;


import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;

import java.util.List;

public interface ParseOfxPort {

    List<RawTransaction> parse(ImportJob job, String ofxContent);

}