package br.com.ecofy.auth.adapters.in.web;

import br.com.ecofy.auth.adapters.in.web.dto.LoginRequest;
import br.com.ecofy.auth.adapters.in.web.dto.RefreshTokenRequest;
import br.com.ecofy.auth.adapters.in.web.dto.TokenResponse;
import br.com.ecofy.auth.core.port.in.AuthenticateUserUseCase;
import br.com.ecofy.auth.core.port.in.RefreshTokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Authentication", description = "Endpoints de autenticação e renovação de tokens JWT/OIDC")
@Slf4j
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase,
                          RefreshTokenUseCase refreshTokenUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Operation(
            summary = "Emite access token e refresh token",
            description = """
                    Endpoint de autenticação baseado em usuário/senha.
                    Segue o fluxo semelhante ao OAuth2 Password/ROPC, retornando access_token e refresh_token.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados faltando ou inválidos)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas (rate limit)"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping(
            path = "/token",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TokenResponse> token(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {

        String clientIp = resolveClientIp(httpRequest);

        log.debug("[AuthController] - [token] -> Autenticando usuário username={} clientId={} ip={}",
                request.username(), request.clientId(), clientIp);

        var result = authenticateUserUseCase.authenticate(
                new AuthenticateUserUseCase.AuthenticationCommand(
                        request.clientId(),
                        request.clientSecret(),
                        request.username(),
                        request.password(),
                        request.scope(),
                        clientIp
                )
        );

        log.debug("[AuthController] - [token] -> Token emitido com sucesso username={} clientId={}",
                request.username(), request.clientId());

        TokenResponse response = new TokenResponse(
                result.tokenType(),
                result.accessToken().value(),
                result.refreshToken(),
                result.expiresInSeconds()
        );

        return ResponseEntity
                .ok()
                .headers(oauthNoStoreHeaders())
                .body(response);
    }

    @Operation(
            summary = "Renova access token usando refresh token",
            description = """
                    Endpoint para renovação do access token.
                    Recebe refresh_token válido e retorna novo access_token (e opcionalmente novo refresh_token).
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido"),
            @ApiResponse(responseCode = "401", description = "Refresh token expirado ou revogado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping(
            path = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {

        log.debug("[AuthController] - [refresh] -> Renovando token clientId={}", request.clientId());

        var result = refreshTokenUseCase.refresh(
                new RefreshTokenUseCase.RefreshTokenCommand(
                        request.clientId(),
                        request.refreshToken(),
                        request.scope()
                )
        );

        log.debug("[AuthController] - [refresh] -> Token renovado com sucesso clientId={}", request.clientId());

        TokenResponse response = new TokenResponse(
                "Bearer",
                result.accessToken(),
                result.refreshToken(),
                result.expiresInSeconds()
        );

        return ResponseEntity
                .ok()
                .headers(oauthNoStoreHeaders())
                .body(response);
    }

    /**
     * Resolve IP real do cliente considerando cabeçalhos de proxy/gateway.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Cabeçalhos recomendados pela RFC 6749 / OAuth2 para o endpoint de token.
     * Impede cache de responses contendo tokens.
     */
    private HttpHeaders oauthNoStoreHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(
                CacheControl.noStore()
                        .mustRevalidate()
                        .cachePrivate()
                        .maxAge(Duration.ZERO)
        );
        headers.add("Pragma", "no-cache");
        return headers;
    }
}
