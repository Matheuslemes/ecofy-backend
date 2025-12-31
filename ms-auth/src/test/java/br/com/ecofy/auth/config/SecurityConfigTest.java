package br.com.ecofy.auth.config;

import br.com.ecofy.auth.adapters.out.jwt.JwtNimbusTokenProviderAdapter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void passwordEncoder_shouldWork() {
        JwtProperties props = new JwtProperties();
        JwtNimbusTokenProviderAdapter adapter = mock(JwtNimbusTokenProviderAdapter.class);

        SecurityConfig config = new SecurityConfig(props, adapter);

        PasswordEncoder encoder = config.passwordEncoder();

        String raw = "pass-123";
        String hash = encoder.encode(raw);

        assertTrue(encoder.matches(raw, hash));
        assertFalse(encoder.matches("wrong", hash));
    }

    @Test
    void jwtDecoder_shouldCreateNimbusJwtDecoder_whenPublicKeyIsAvailable() throws Exception {
        JwtProperties props = new JwtProperties();
        props.setIssuer("https://issuer.test");

        JwtNimbusTokenProviderAdapter adapter = mock(JwtNimbusTokenProviderAdapter.class);
        when(adapter.toRsaJwk()).thenReturn(rsaKey());

        SecurityConfig config = new SecurityConfig(props, adapter);

        JwtDecoder decoder = config.jwtDecoder();

        assertNotNull(decoder);
        assertInstanceOf(NimbusJwtDecoder.class, decoder);

        verify(adapter).toRsaJwk();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    void jwtDecoder_shouldThrowIllegalStateException_whenToRSAPublicKeyThrowsJoseException() throws Exception {
        JwtProperties props = new JwtProperties();

        JwtNimbusTokenProviderAdapter adapter = mock(JwtNimbusTokenProviderAdapter.class);

        RSAKey rsaKey = mock(RSAKey.class);
        when(adapter.toRsaJwk()).thenReturn(rsaKey);
        when(rsaKey.toRSAPublicKey()).thenThrow(new JOSEException("boom"));

        SecurityConfig config = new SecurityConfig(props, adapter);

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::jwtDecoder);
        assertEquals("Could not create JwtDecoder from in-memory JWK", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());

        verify(adapter).toRsaJwk();
        verify(rsaKey).toRSAPublicKey();
        verifyNoMoreInteractions(adapter, rsaKey);
    }

    @Test
    void jwtValidatorFactory_shouldValidateIssuer_whenIssuerProvided() {
        JwtProperties props = new JwtProperties();
        props.setIssuer("https://issuer.ok");

        SecurityConfig.JwtValidatorFactory factory = new SecurityConfig.JwtValidatorFactory(props);
        OAuth2TokenValidator<Jwt> validator = factory.create();

        Instant now = Instant.now();

        Jwt ok = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("iss", URI.create("https://issuer.ok"))
                .issuedAt(now.minusSeconds(5))
                .expiresAt(now.plusSeconds(60))
                .build();

        OAuth2TokenValidatorResult okRes = validator.validate(ok);
        assertFalse(okRes.hasErrors());

        Jwt bad = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("iss", URI.create("https://issuer.bad"))
                .issuedAt(now.minusSeconds(5))
                .expiresAt(now.plusSeconds(60))
                .build();

        OAuth2TokenValidatorResult badRes = validator.validate(bad);
        assertTrue(badRes.hasErrors());
    }

    @Test
    void jwtValidatorFactory_shouldNotEnforceIssuer_whenIssuerNull() {
        JwtProperties props = new JwtProperties();

        SecurityConfig.JwtValidatorFactory factory = new SecurityConfig.JwtValidatorFactory(props);
        OAuth2TokenValidator<Jwt> validator = factory.create();

        Instant now = Instant.now();

        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("iss", URI.create("https://any-issuer"))
                .issuedAt(now.minusSeconds(5))
                .expiresAt(now.plusSeconds(60))
                .build();

        OAuth2TokenValidatorResult res = validator.validate(jwt);
        assertFalse(res.hasErrors());
    }

    private static RSAKey rsaKey() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        return new RSAKey.Builder(pub).keyID("kid-1").build();
    }
}
