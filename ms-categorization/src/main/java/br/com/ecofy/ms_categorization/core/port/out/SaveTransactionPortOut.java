package br.com.ecofy.ms_categorization.core.port.out;

import br.com.ecofy.ms_categorization.core.domain.Transaction;

public interface SaveTransactionPortOut {

    Transaction save(Transaction transaction);


}
