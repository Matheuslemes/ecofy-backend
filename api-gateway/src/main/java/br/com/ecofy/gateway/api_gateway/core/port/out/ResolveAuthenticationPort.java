package br.com.ecofy.gateway.api_gateway.core.port.out;

import br.com.ecofy.gateway.api_gateway.core.domain.TenantContext;

public interface ResolveAuthenticationPort {

    TenantContext resolveFromBearerToken(String bearerToken);

    default TenantContext tryResolve(String bearerToken) {
        try {
            return resolveFromBearerToken(bearerToken);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}