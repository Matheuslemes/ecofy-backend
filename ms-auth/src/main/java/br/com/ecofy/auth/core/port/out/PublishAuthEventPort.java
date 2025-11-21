package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.event.*;

public interface PublishAuthEventPort {

    void publish(UserRegisteredEvent event);

    void publish(UserEmailConfirmedEvent event);

    void publish(UserAuthenticatedEvent event);

    void publish(PasswordResetRequestedEvent event);

}
