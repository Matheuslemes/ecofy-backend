package br.com.ecofy.gateway.api_gateway.core.application.service;

import br.com.ecofy.gateway.api_gateway.core.domain.RouteDefinition;
import br.com.ecofy.gateway.api_gateway.core.port.in.GetRoutesUseCase;
import br.com.ecofy.gateway.api_gateway.core.port.in.ReloadRoutesUseCase;
import br.com.ecofy.gateway.api_gateway.core.port.out.LoadRouteDefinitionsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class RouteService implements GetRoutesUseCase, ReloadRoutesUseCase {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final LoadRouteDefinitionsPort loadRouteDefinitionsPort;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Map<String, RouteDefinition> routesById = new HashMap<>();
    private long cachedVersion = -1L;
    private Instant lastReloadAt = Instant.EPOCH;

    public RouteService(
            @Qualifier("staticYamlRouteDefinitionsAdapter")
            LoadRouteDefinitionsPort loadRouteDefinitionsPort
    ) {
        this.loadRouteDefinitionsPort = loadRouteDefinitionsPort;
        internalReload();
    }

    @Override
    public List<RouteDefinition> getAllActiveRoutes() {

        lock.readLock().lock();
        try {
            long currentVersion = loadRouteDefinitionsPort.getCurrentVersion();
            if (currentVersion > cachedVersion) {
                log.info("[RouteService] Detected route version change: {} -> {}, triggering reload",
                        cachedVersion, currentVersion);
                lock.readLock().unlock();
                reloadAll();
                lock.readLock().lock();
            }
            return routesById.values()
                    .stream()
                    .filter(RouteDefinition::enabled)
                    .sorted(Comparator.comparingInt(RouteDefinition::order))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void reloadAll() {
        lock.writeLock().lock();
        try {
            internalReload();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void reloadRoute(String routeId) {
        lock.writeLock().lock();
        try {
            log.info("[RouteService] Reloading single route: {}", routeId);
            loadRouteDefinitionsPort.findById(routeId)
                    .ifPresentOrElse(
                            rd -> routesById.put(rd.id(), rd),
                            () -> routesById.remove(routeId)
                    );
            this.cachedVersion = loadRouteDefinitionsPort.getCurrentVersion();
            this.lastReloadAt = Instant.now();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void internalReload() {
        log.info("[RouteService] Reloading all routes from storage...");
        List<RouteDefinition> loaded = loadRouteDefinitionsPort.loadAll();
        Map<String, RouteDefinition> newMap = new HashMap<>();
        for (RouteDefinition rd : loaded) {
            newMap.put(rd.id(), rd);
        }
        this.routesById = newMap;
        this.cachedVersion = loadRouteDefinitionsPort.getCurrentVersion();
        this.lastReloadAt = Instant.now();

        log.info("[RouteService] Reloaded {} routes. version={}, lastReloadAt={}",
                loaded.size(), cachedVersion, lastReloadAt);
    }

    public Optional<RouteDefinition> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(routesById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Instant getLastReloadAt() {
        return lastReloadAt;
    }

}
