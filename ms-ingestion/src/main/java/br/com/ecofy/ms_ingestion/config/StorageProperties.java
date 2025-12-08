package br.com.ecofy.ms_ingestion.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ecofy.ingestion.storage")
@Getter
@Setter
@ToString
public class StorageProperties {

    //Diretório/basePath local onde os arquivos serão armazenados.
    // Ex: /var/lib/ecofy/ingestion-files ou ./data/ingestion
    private String basePath = "./data/ingestion";

    // Tamanho máximo permitido por arquivo (em bytes).
    private long maxFileSizeBytes = 50 * 1024 * 1024; // 50 MB

    // Flag para deletar o arquivo após o processamento.
    private boolean deleteOnSuccess = false;

}