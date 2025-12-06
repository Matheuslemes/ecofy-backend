package br.com.ecofy.ms_ingestion.core.port.in;

public interface RetryFailedImportsUseCase {

    record RetryFailedImportsCommand(int maxJobs) {}

    void retry(RetryFailedImportsCommand command);

}
