package br.com.ecofy.auth.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtPropertiesTest {

    @Test
    void gettersAndSetters_shouldWork_andConstantsShouldHaveExpectedDefaults() {
        JwtProperties p = new JwtProperties();

        assertNull(p.getIssuer());
        assertNull(p.getAudience());
        assertNull(p.getKeyId());
        assertNull(p.getPrivateKeyLocation());
        assertNull(p.getPublicKeyLocation());

        p.setIssuer("https://issuer.test");
        p.setAudience("aud-test");
        p.setKeyId("kid-1");
        p.setPrivateKeyLocation("classpath:keys/private.pem");
        p.setPublicKeyLocation("classpath:keys/public.pem");

        assertEquals("https://issuer.test", p.getIssuer());
        assertEquals("aud-test", p.getAudience());
        assertEquals("kid-1", p.getKeyId());
        assertEquals("classpath:keys/private.pem", p.getPrivateKeyLocation());
        assertEquals("classpath:keys/public.pem", p.getPublicKeyLocation());

        assertEquals(900L, p.getAccessTokenTtlSeconds());
        assertEquals(60L * 60L * 24L * 30L, p.getRefreshTokenTtlSeconds());
        assertEquals(60L, p.getClockSkewSeconds());
    }
}
