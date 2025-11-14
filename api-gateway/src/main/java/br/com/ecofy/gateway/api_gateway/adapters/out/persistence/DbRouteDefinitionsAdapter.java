package br.com.ecofy.gateway.api_gateway.adapters.out.persistence;

import br.com.ecofy.gateway.api_gateway.core.domain.RouteDefinition;
import br.com.ecofy.gateway.api_gateway.core.port.out.LoadRouteDefinitionsPort;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Profile("db-routes") 
@Component
public class DbRouteDefinitionsAdapter implements LoadRouteDefinitionsPort {

    private static final Logger log = LoggerFactory.getLogger(DbRouteDefinitionsAdapter.class);

    private final RouteDefinitionJpaRepository repository;

    public DbRouteDefinitionsAdapter(RouteDefinitionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RouteDefinition> loadAll() {
        log.info("[DbRouteDefinitionsAdapter] Loading all routes from database...");
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<RouteDefinition> findById(String id) {
        log.info("[DbRouteDefinitionsAdapter] Loading route by id from database: {}", id);
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public long getCurrentVersion() {
        return repository.maxVersion().orElse(0L);
    }

    private RouteDefinition toDomain(RouteDefinitionEntity e) {
        return RouteDefinition.builder()
                .id(e.getId())
                .pathPattern(e.getPathPattern())
                .httpMethods(Set.copyOf(e.getHttpMethods()))
                .upstreamUri(URI.create(e.getUpstreamUri()))
                .order(e.getRouteOrder())
                .enabled(e.isEnabled())
                .tenantsAllowed(Set.copyOf(e.getTenantsAllowed()))
                .rateLimitPolicyId(e.getRateLimitPolicyId())
                .metadata(e.getMetadata())
                .predicates(e.getPredicates())
                .filters(e.getFilters())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .version(e.getVersion())
                .build();
    }

    @Entity
    @Table(name = "gateway_route_definition")
    public static class RouteDefinitionEntity {

        @Id
        private String id;

        @Column(name = "path_pattern", nullable = false)
        private String pathPattern;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "gateway_route_methods", joinColumns = @JoinColumn(name = "route_id"))
        @Column(name = "http_method")
        private List<String> httpMethods;

        @Column(name = "upstream_uri", nullable = false)
        private String upstreamUri;

        @Column(name = "route_order")
        private int routeOrder;

        @Column(name = "enabled")
        private boolean enabled = true;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "gateway_route_tenants", joinColumns = @JoinColumn(name = "route_id"))
        @Column(name = "tenant_id")
        private List<String> tenantsAllowed;

        @Column(name = "rate_limit_policy_id")
        private String rateLimitPolicyId;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "gateway_route_metadata", joinColumns = @JoinColumn(name = "route_id"))
        @MapKeyColumn(name = "meta_key")
        @Column(name = "meta_value")
        private java.util.Map<String, String> metadata;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "gateway_route_predicates", joinColumns = @JoinColumn(name = "route_id"))
        @Column(name = "predicate_expression")
        private List<String> predicates;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "gateway_route_filters", joinColumns = @JoinColumn(name = "route_id"))
        @Column(name = "filter_expression")
        private List<String> filters;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @Column(name = "version", nullable = false)
        private long version;

        public String getId() { return id; }
        public String getPathPattern() { return pathPattern; }
        public List<String> getHttpMethods() { return httpMethods; }
        public String getUpstreamUri() { return upstreamUri; }
        public int getRouteOrder() { return routeOrder; }
        public boolean isEnabled() { return enabled; }
        public List<String> getTenantsAllowed() { return tenantsAllowed; }
        public String getRateLimitPolicyId() { return rateLimitPolicyId; }
        public java.util.Map<String, String> getMetadata() { return metadata; }
        public List<String> getPredicates() { return predicates; }
        public List<String> getFilters() { return filters; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
        public long getVersion() { return version; }

    }

    public interface RouteDefinitionJpaRepository extends JpaRepository<RouteDefinitionEntity, String> {

        @org.springframework.data.jpa.repository.Query(
                "select max(r.version) from DbRouteDefinitionsAdapter$RouteDefinitionEntity r"
        )
        java.util.Optional<Long> maxVersion();
    }

}
