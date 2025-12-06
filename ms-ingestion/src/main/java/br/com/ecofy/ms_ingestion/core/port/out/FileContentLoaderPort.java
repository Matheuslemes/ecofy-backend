package br.com.ecofy.ms_ingestion.core.port.out;

public interface FileContentLoaderPort {

    byte[] load(String path);

}
