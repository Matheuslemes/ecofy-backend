package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.AuthUser;

public interface SendResetPasswordEmailPort {

    void sendReset(AuthUser user, String resetToken);

}
