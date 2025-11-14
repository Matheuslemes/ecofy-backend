package br.com.ecofy.gateway.api_gateway.core.port.in;


public interface ReloadRoutesUseCase {

    void reloadAll();

    default void reloadRoute(String routeId) {}

}