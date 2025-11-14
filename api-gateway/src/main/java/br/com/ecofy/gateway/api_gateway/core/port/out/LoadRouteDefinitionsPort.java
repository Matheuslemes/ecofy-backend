package br.com.ecofy.gateway.api_gateway.core.port.out;

import br.com.ecofy.gateway.api_gateway.core.domain.RouteDefinition;

import java.util.List;
import java.util.Optional;

public interface LoadRouteDefinitionsPort {

    List<RouteDefinition> loadAll();

    default List<RouteDefinition> loadAllActive() {
        return loadAll().stream()
                .filter(RouteDefinition::enabled)
                .toList();
    }

    Optional<RouteDefinition> findById(String id);

    default long getCurrentVersion() {
        return loadAll().stream()
                .mapToLong(RouteDefinition::version)
                .max()
                .orElse(0L);
    }

}