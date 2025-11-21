package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.valueobject.PasswordHash;

public interface PasswordHashingPort {

    PasswordHash hash(String rawPassword);

    boolean matches(String rawPassword, PasswordHash hash);

}
