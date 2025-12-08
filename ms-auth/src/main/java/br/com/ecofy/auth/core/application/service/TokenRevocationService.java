package br.com.ecofy.auth.core.application.service;

import br.com.ecofy.auth.core.application.exception.AuthErrorCode;
import br.com.ecofy.auth.core.application.exception.AuthException;
import br.com.ecofy.auth.core.port.in.RevokeTokenUseCase;
import br.com.ecofy.auth.core.port.out.RefreshTokenStorePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

// Serviço responsável por revogar tokens emitidos pelo MS Auth.
@Slf4j
@Service
public class TokenRevocationService implements RevokeTokenUseCase {

    private final RefreshTokenStorePort refreshTokenStorePort;

    public TokenRevocationService(RefreshTokenStorePort refreshTokenStorePort) {
        this.refreshTokenStorePort =
                Objects.requireNonNull(refreshTokenStorePort, "refreshTokenStorePort must not be null");
    }

    @Override
    public void revoke(RevokeTokenCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        String masked = maskToken(command.token());

        log.debug(
                "[TokenRevocationService] - [revoke] -> Iniciando revogação de token={} isRefreshToken={}",
                masked, command.refreshToken()
        );

        if (!command.refreshToken()) {
            log.warn(
                    "[TokenRevocationService] - [revoke] -> Tipo de token não suportado para revogação tokenMask={}",
                    masked
            );
            throw new AuthException(
                    AuthErrorCode.TOKEN_TYPE_NOT_SUPPORTED_FOR_REVOCATION,
                    "Only refresh tokens can be revoked"
            );
        }

        if (command.refreshToken()) {
            refreshTokenStorePort.revoke(command.token());
            log.debug(
                    "[TokenRevocationService] - [revoke] -> Refresh token revogado tokenMask={}",
                    masked
            );
        }

        log.debug(
                "[TokenRevocationService] - [revoke] -> Processo concluído tokenMask={}",
                masked
        );
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) return "***";
        return token.length() > 10 ? token.substring(0, 10) + "..." : "***";
    }
}
