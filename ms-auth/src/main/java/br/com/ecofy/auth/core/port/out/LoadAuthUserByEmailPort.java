package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.AuthUser;
import br.com.ecofy.auth.core.domain.valueobject.EmailAddress;

import java.util.Optional;

public interface LoadAuthUserByEmailPort {

    Optional<AuthUser> loadByEmail(EmailAddress email);

}
