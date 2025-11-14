package br.com.ecofy.gateway.api_gateway.adapters.in.web;

import br.com.ecofy.gateway.api_gateway.core.domain.RouteDefinition;
import br.com.ecofy.gateway.api_gateway.core.port.in.GetRoutesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GatewayRouteConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteConfiguration.class);

    private final GetRoutesUseCase getRoutesUseCase;

    public GatewayRouteConfiguration(GetRoutesUseCase getRoutesUseCase) {
        this.getRoutesUseCase = getRoutesUseCase;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        log.info("[GatewayRouteConfiguration] Building routes from RouteService...");

        RouteLocatorBuilder.Builder routes = builder.routes();
        List<RouteDefinition> definitions = getRoutesUseCase.getAllActiveRoutes();

        for (RouteDefinition rd : definitions) {
            routes.route(rd.id(), r ->
                    r.path(rd.pathPattern())
                            .uri(rd.upstreamUri())
            );
        }

        return routes.build();
    }
}