package br.com.ecofy.gateway.api_gateway.adapters.out.persistence;

import br.com.ecofy.gateway.api_gateway.core.domain.RouteDefinition;
import br.com.ecofy.gateway.api_gateway.core.port.out.LoadRouteDefinitionsPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Primary
@Component
public class StaticYamlRouteDefinitionsAdapter implements LoadRouteDefinitionsPort {

    private static final Logger log = LoggerFactory.getLogger(StaticYamlRouteDefinitionsAdapter.class);

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private final AtomicLong versionGenerator = new AtomicLong(1);

    public StaticYamlRouteDefinitionsAdapter(ResourceLoader resourceLoader,
                                             ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RouteDefinition> loadAll() {

        try {
            Resource resource = resourceLoader.getResource("classpath:gateway-routes.json");
            if (!resource.exists()) {
                log.warn("[StaticYamlRouteDefinitionsAdapter] gateway-routes.json not found, returning empty list.");
                return Collections.emptyList();
            }

            List<Map<String, Object>> raw = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );

            return raw.stream()
                    .map(this::toDomain)
                    .toList();
        } catch (Exception e) {
            log.error("[StaticYamlRouteDefinitionsAdapter] Failed to load routes from static JSON/YAML", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<RouteDefinition> findById(String id) {
        return loadAll().stream()
                .filter(rd -> rd.id().equals(id))
                .findFirst();
    }

    @Override
    public long getCurrentVersion() {
        return versionGenerator.get();

    }

    private RouteDefinition toDomain(Map<String, Object> map) {

        String id = (String) map.get("id");
        String path = (String) map.getOrDefault("path", "/**");
        String uri = (String) map.getOrDefault("uri", "http://localhost:8080");
        List<String> methods = (List<String>) map.getOrDefault("methods", List.of("GET", "POST", "PUT", "DELETE"));
        List<String> predicates = (List<String>) map.getOrDefault("predicates", List.of("Path=" + path));
        List<String> filters = (List<String>) map.getOrDefault("filters", List.of("StripPrefix=1"));

        return RouteDefinition.builder()
                .id(id)
                .pathPattern(path)
                .httpMethods(Set.copyOf(methods))
                .upstreamUri(URI.create(uri))
                .predicates(predicates)
                .filters(filters)
                .build();
    }
}