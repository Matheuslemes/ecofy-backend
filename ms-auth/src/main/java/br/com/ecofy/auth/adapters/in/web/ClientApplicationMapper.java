package br.com.ecofy.auth.adapters.in.web;

import br.com.ecofy.auth.adapters.in.web.dto.ClientApplicationResponse;
import br.com.ecofy.auth.core.domain.ClientApplication;
import br.com.ecofy.auth.core.domain.enums.GrantType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter {@link ClientApplication} (domínio)
 * em {@link ClientApplicationResponse} (DTO exposto na API REST).
 */
final class ClientApplicationMapper {

    private ClientApplicationMapper() {
        // utility class
    }

    /**
     * Converte um {@link ClientApplication} para {@link ClientApplicationResponse}.
     *
     * @param client agregado de domínio (não pode ser nulo)
     * @return DTO pronto para ser retornado pela API
     */
    static ClientApplicationResponse toResponse(ClientApplication client) {
        Objects.requireNonNull(client, "client must not be null");

        String id = client.id() != null
                ? client.id().toString()
                : UUID.randomUUID().toString(); // ou null, se preferir não gerar

        Set<GrantType> grants = safeGrants(client.grantTypes());
        Set<String> redirectUris = safeStrings(client.redirectUris());
        Set<String> scopes = safeStrings(client.scopes());

        return new ClientApplicationResponse(
                id,
                client.clientId(),
                client.name(),
                client.clientType(),
                grants,
                redirectUris,
                scopes,
                client.isFirstParty(),
                client.isActive(),
                client.createdAt(),
                client.updatedAt()
        );
    }

    /**
     * Converte uma lista de {@link ClientApplication} para lista imutável
     * de {@link ClientApplicationResponse}.
     */
    static List<ClientApplicationResponse> toResponseList(List<ClientApplication> clients) {
        if (clients == null || clients.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(
                clients.stream()
                        .filter(Objects::nonNull)
                        .map(ClientApplicationMapper::toResponse)
                        .toList()
        );
    }

    // helpers

    private static Set<String> safeStrings(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<GrantType> safeGrants(Set<GrantType> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
