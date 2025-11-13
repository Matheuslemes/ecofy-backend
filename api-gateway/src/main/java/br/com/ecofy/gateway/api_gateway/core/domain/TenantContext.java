package br.com.ecofy.gateway.api_gateway.core.domain;

import java.util.Map;
import java.util.Set;

public final class TenantContext {

    private final String tenantId;

    private final String subject;    // userId / sub

    private final Set<String> roles;

    private final Set<String> scopes;

    private final String traceId;

    private final String correlationId;

    private final Map<String, Object> attributes;

    public TenantContext(String tenantId,
                         String subject,
                         Set<String> roles,
                         Set<String> scopes,
                         String traceId,
                         String correlationId,
                         Map<String, Object> attributes) {
        this.tenantId = tenantId;
        this.subject = subject;
        this.roles = Set.copyOf(roles);
        this.scopes = Set.copyOf(scopes);
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.attributes = Map.copyOf(attributes);
    }

    public String tenantId() { return tenantId; }
    public String subject() { return subject; }
    public Set<String> roles() { return roles; }
    public Set<String> scopes() { return scopes; }
    public String traceId() { return traceId; }
    public String correlationId() { return correlationId; }
    public Map<String, Object> attributes() { return attributes; }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasScope(String scope) {
        return scopes.contains(scope);
    }

    public Object attribute(String key) {
        return attributes.get(key);
    }

    public TenantContext withAttribute(String key, Object value) {
        var newAttrs = new java.util.HashMap<>(attributes);
        newAttrs.put(key, value);
        return new TenantContext(
                tenantId,
                subject,
                roles,
                scopes,
                traceId,
                correlationId,
                newAttrs
        );
    }

    @Override
    public String toString() {
        return "TenantContext{" +
                "tenantId='" + tenantId + '\'' +
                ", subject='" + subject + '\'' +
                ", roles=" + roles +
                ", scopes=" + scopes +
                ", traceId='" + traceId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }

}