package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.AuthUser;

import java.util.Optional;

public interface
VerificationTokenStorePort {

    void store(AuthUser user, String token);

    Optional<AuthUser> consume(String token);

}
