package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.AuthUser;
import br.com.ecofy.auth.core.domain.valueobject.AuthUserId;

import java.util.Optional;

public interface LoadAuthUserByIdPort {

    Optional<AuthUser> loadById(AuthUserId id);

}