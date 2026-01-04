package br.com.ecofy.ms_ingestion.core.application.exception;

public class EmptyTransactionsPayloadException extends IngestionException {

    public EmptyTransactionsPayloadException(String sourceSystem, String payloadId) {
        super(
                IngestionErrorCode.EMPTY_TRANSACTIONS_PAYLOAD,
                "No transactions in payload",
                "sourceSystem=" + sourceSystem + ", payloadId=" + payloadId
        );
    }

}
