package br.com.ecofy.auth.adapters.in.web;

import br.com.ecofy.auth.adapters.in.web.dto.UserResponse;
import br.com.ecofy.auth.core.domain.AuthUser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter objetos de domínio {@link AuthUser}
 * para DTOs expostos nas APIs REST ({@link UserResponse}).
 */
final class UserMapper {

    private UserMapper() {
        // util class
    }

    /**
     * Converte um {@link AuthUser} para {@link UserResponse}.
     *
     * @param user usuário de domínio (não pode ser nulo)
     * @return DTO pronto para ser retornado pela API
     */
    static UserResponse toResponse(AuthUser user) {
        Objects.requireNonNull(user, "user must not be null");

        Set<String> roles = user.roles().stream()
                .map(br.com.ecofy.auth.core.domain.Role::name)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> permissions = user.directPermissions().stream()
                .map(br.com.ecofy.auth.core.domain.Permission::name)
                .collect(Collectors.toUnmodifiableSet());

        return new UserResponse(
                user.id().value().toString(),
                user.email().value(),
                user.fullName(),
                user.status().name(),
                user.isEmailVerified(),
                roles,
                permissions,
                user.createdAt(),
                user.updatedAt(),
                user.lastLoginAt()
        );
    }

    /**
     * Converte uma lista de {@link AuthUser} para uma lista de {@link UserResponse}.
     *
     * @param users lista de usuários (pode ser nula, será tratada como vazia)
     * @return lista imutável de DTOs
     */
    static List<UserResponse> toResponseList(List<AuthUser> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(
                users.stream()
                        .filter(Objects::nonNull)
                        .map(UserMapper::toResponse)
                        .toList()
        );
    }
}
