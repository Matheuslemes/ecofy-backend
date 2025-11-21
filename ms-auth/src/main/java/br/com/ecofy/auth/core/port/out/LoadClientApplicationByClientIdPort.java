package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.ClientApplication;

import java.util.Optional;

public interface LoadClientApplicationByClientIdPort {

    Optional<ClientApplication> loadByClientId(String clientId);

}