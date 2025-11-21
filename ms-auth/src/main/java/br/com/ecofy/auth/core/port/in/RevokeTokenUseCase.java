package br.com.ecofy.auth.core.port.in;

public interface RevokeTokenUseCase {

    void revoke(RevokeTokenCommand command);

    record RevokeTokenCommand(

            String clientId,

            String token,

            boolean refreshToken

    ) { }

}
