package br.com.ecofy.ms_budgeting.core.port.out;

import java.util.UUID;

public interface DeleteBudgetPort {

    void deleteById(UUID id);
    boolean existsById(UUID id);

}
