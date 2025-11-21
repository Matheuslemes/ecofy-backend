package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenStorePort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void revoke(String tokenValue);

}