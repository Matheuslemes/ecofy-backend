package br.com.ecofy.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    // issuer padrao para todos os tokens (iss)
    private String issuer;

    // audience padrao (aud), usado para validar consumidores
    private String audience;

    // identificador de chave no JWKS (kid)
    private String keyId;

    // localização do arquivo pem com a chave publica rsa (classpath ou file:)
    private String privateKeyLocation;

    // localizacao do arquivo PEM COM A chave pública RSA (classpath: ou files)
    private String publicKeyLocation;

    // tempo de vida do refresh token em segundos
    private final Long accessTokenTllSeconds = 900L; // 15min

    // tempo de vida do refresg token em segundos
    private final Long refreshTokenTtlSeconds = 60L * 60L * 24L * 30L; // 30 dias

    // clock skew permitido para validação de tokens (segundos)
    private final Long clockSkewSeconds = 60L;


    public String getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPublicKeyLocation(String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(@DefaultValue("https://auth.ecofy.com") String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(@DefaultValue("ecofy-api") String audience) {
        this.audience = audience;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getPrivateKeyLocation() {
        return privateKeyLocation;
    }

    public void setPrivateKeyLocation(String privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public Long getAccessTokenTllSeconds() {
        return accessTokenTllSeconds;
    }

    public Long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlSeconds;
    }

    public Long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

}
