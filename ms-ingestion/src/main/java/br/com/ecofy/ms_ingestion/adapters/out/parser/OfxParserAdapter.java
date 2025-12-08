package br.com.ecofy.ms_ingestion.adapters.out.parser;

import br.com.ecofy.ms_ingestion.core.domain.ImportJob;
import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.port.out.ParseOfxPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OfxParserAdapter implements ParseOfxPort {

    @Override
    public List<RawTransaction> parse(ImportJob job, String ofxContent) {
        log.info("[OfxParserAdapter] - [parse] -> Parsing OFX para jobId={}", job.id());
        // Aqui entraria um parser OFX real; vou deixar um stub que extrai zero registros,
        // pronto para plugar uma lib (ex: ofx4j).
        return new ArrayList<>();
    }

}