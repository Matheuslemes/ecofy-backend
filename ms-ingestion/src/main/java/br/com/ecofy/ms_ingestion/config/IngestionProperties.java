package br.com.ecofy.ms_ingestion.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ecofy.ingestion")
@Getter
@Setter
@ToString
public class IngestionProperties {

    // Número máximo de erros tolerados por job antes de marcá-lo como FAILED.
    private int maxErrorsPerJob = 500;

    // Lote de flush de RawTransactions por vez (para reduzir pressão no banco).
    private int batchSize = 500;

    // Quantidade máxima de jobs em estado PENDING a serem reprocessados numa chamada de retry.
    private int maxJobsPerRetry = 10;

}