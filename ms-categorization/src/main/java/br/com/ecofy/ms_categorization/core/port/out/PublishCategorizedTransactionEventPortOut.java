package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.adapters.out.messaging.dto.CategorizationAppliedEvent;
import br.com.ecofy.ms_categorization.adapters.out.messaging.dto.CategorizedTransactionEvent;

public interface PublishCategorizedTransactionEventPortOut {

    void publish(CategorizedTransactionEvent event);

    void publish(CategorizationAppliedEvent event);

}
