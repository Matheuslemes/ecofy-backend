package br.com.ecofy.gateway.api_gateway.core.domain;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RouteDefinition {

    private final String id;

    private final String pathPattern;

    private final Set<String> httpMethods;

    private final URI upstreamUri;

    private final int order;

    private final boolean enabled;

    private final Set<String> tenantsAllowed;

    private final String rateLimitPolicyId;

    private final Map<String, String> metadata;

    private final List<String> predicates;

    private final List<String> filters;

    private final Instant createdAt;

    private final Instant updatedAt;

    private final long version;

    private RouteDefinition(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.pathPattern = Objects.requireNonNull(builder.pathPattern, "pathPattern is required");
        this.httpMethods = Set.copyOf(builder.httpMethods);
        this.upstreamUri = Objects.requireNonNull(builder.upstreamUri, "upstreamUri is required");
        this.order = builder.order;
        this.enabled = builder.enabled;
        this.tenantsAllowed = Set.copyOf(builder.tenantsAllowed);
        this.rateLimitPolicyId = builder.rateLimitPolicyId;
        this.metadata = Map.copyOf(builder.metadata);
        this.predicates = List.copyOf(builder.predicates);
        this.filters = List.copyOf(builder.filters);
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
    }

    public String id() { return id; }
    public String pathPattern() { return pathPattern; }
    public Set<String> httpMethods() { return httpMethods; }
    public URI upstreamUri() { return upstreamUri; }
    public int order() { return order; }
    public boolean enabled() { return enabled; }
    public Set<String> tenantsAllowed() { return tenantsAllowed; }
    public String rateLimitPolicyId() { return rateLimitPolicyId; }
    public Map<String, String> metadata() { return metadata; }
    public List<String> predicates() { return predicates; }
    public List<String> filters() { return filters; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public long version() { return version; }

    public RouteDefinition withUpdatedAt(Instant updatedAt, long version) {
        return builder()
                .id(id)
                .pathPattern(pathPattern)
                .httpMethods(httpMethods)
                .upstreamUri(upstreamUri)
                .order(order)
                .enabled(enabled)
                .tenantsAllowed(tenantsAllowed)
                .rateLimitPolicyId(rateLimitPolicyId)
                .metadata(metadata)
                .predicates(predicates)
                .filters(filters)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .version(version)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String pathPattern;
        private Set<String> httpMethods = Set.of();
        private URI upstreamUri;
        private int order = 0;
        private boolean enabled = true;
        private Set<String> tenantsAllowed = Set.of();
        private String rateLimitPolicyId;
        private Map<String, String> metadata = Map.of();
        private List<String> predicates = List.of();
        private List<String> filters = List.of();
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();
        private long version = 0L;

        public Builder id(String id) { this.id = id; return this; }
        public Builder pathPattern(String pathPattern) { this.pathPattern = pathPattern; return this; }
        public Builder httpMethods(Set<String> httpMethods) { this.httpMethods = httpMethods; return this; }
        public Builder upstreamUri(URI upstreamUri) { this.upstreamUri = upstreamUri; return this; }
        public Builder order(int order) { this.order = order; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder tenantsAllowed(Set<String> tenantsAllowed) { this.tenantsAllowed = tenantsAllowed; return this; }
        public Builder rateLimitPolicyId(String rateLimitPolicyId) { this.rateLimitPolicyId = rateLimitPolicyId; return this; }
        public Builder metadata(Map<String, String> metadata) { this.metadata = metadata; return this; }
        public Builder predicates(List<String> predicates) { this.predicates = predicates; return this; }
        public Builder filters(List<String> filters) { this.filters = filters; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder version(long version) { this.version = version; return this; }

        public RouteDefinition build() {
            return new RouteDefinition(this);
        }
    }
}