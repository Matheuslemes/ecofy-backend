package br.com.ecofy.ms_categorization.core.port.in;


import br.com.ecofy.ms_categorization.core.application.result.SuggestionResult;

import java.util.UUID;

public interface GetSuggestionUseCase {

    SuggestionResult getByTransactionId(UUID transactionId);

}