package br.com.ecofy.gateway.api_gateway.core.port.in;


import br.com.ecofy.gateway.api_gateway.core.domain.RouteDefinition;

import java.util.List;


public interface GetRoutesUseCase {

    List<RouteDefinition> getAllActiveRoutes();

    default List<RouteDefinition> getRoutesForTenant(String tenantId) {
        return getAllActiveRoutes().stream()
                .filter(rd -> rd.tenantsAllowed().isEmpty() || rd.tenantsAllowed().contains(tenantId))
                .toList();
    }

}