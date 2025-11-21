package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.JwkKey;

import java.util.List;

public interface JwksRepositoryPort {

    List<JwkKey> findActiveSigningKeys();

}
