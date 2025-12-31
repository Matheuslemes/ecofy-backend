package br.com.ecofy.auth.adapters.out.verification;

import br.com.ecofy.auth.core.domain.AuthUser;
import br.com.ecofy.auth.core.domain.valueobject.AuthUserId;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryVerificationTokenStoreAdapterTest {

    @Test
    void store_shouldIgnore_whenUserIsNull() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        adapter.store(null, "token-1");

        assertTrue(adapter.consume("token-1").isEmpty());
    }

    @Test
    void store_shouldIgnore_whenTokenIsNull() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        adapter.store(mockUser(), null);

        assertTrue(adapter.consume("any").isEmpty());
    }

    @Test
    void store_shouldIgnore_whenTokenIsBlank() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        adapter.store(mockUser(), "   ");

        assertTrue(adapter.consume("   ").isEmpty());
    }

    @Test
    void store_shouldStoreToken_andConsumeShouldReturnUser_andRemoveToken() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        AuthUser user = mockUser();
        String token = "token-12345";

        adapter.store(user, token);

        Optional<AuthUser> first = adapter.consume(token);
        assertTrue(first.isPresent());
        assertSame(user, first.get());

        Optional<AuthUser> second = adapter.consume(token);
        assertTrue(second.isEmpty());
    }

    @Test
    void consume_shouldReturnEmpty_whenTokenIsNull() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        assertTrue(adapter.consume(null).isEmpty());
    }

    @Test
    void consume_shouldReturnEmpty_whenTokenIsBlank() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        assertTrue(adapter.consume("  ").isEmpty());
    }

    @Test
    void consume_shouldReturnEmpty_whenTokenNotFound() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        assertTrue(adapter.consume("missing-token").isEmpty());
    }

    @Test
    void store_shouldOverwriteExistingToken_withNewUser() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        AuthUser user1 = mockUser();
        AuthUser user2 = mockUser();
        String token = "same-token";

        adapter.store(user1, token);
        adapter.store(user2, token);

        Optional<AuthUser> consumed = adapter.consume(token);
        assertTrue(consumed.isPresent());
        assertSame(user2, consumed.get());

        assertTrue(adapter.consume(token).isEmpty());
    }

    @Test
    void tokenMaskBranches_shouldBeCovered_viaStoreAndConsumePaths() {
        InMemoryVerificationTokenStoreAdapter adapter = new InMemoryVerificationTokenStoreAdapter();

        AuthUser user = mockUser();

        String shortToken = "1234567890";
        adapter.store(user, shortToken);
        assertTrue(adapter.consume(shortToken).isPresent());

        String longToken = "12345678901";
        adapter.store(user, longToken);
        assertTrue(adapter.consume(longToken).isPresent());

        assertTrue(adapter.consume("not-present-long-token-123456").isEmpty());
    }

    private static AuthUser mockUser() {
        AuthUser user = mock(AuthUser.class);
        AuthUserId id = mock(AuthUserId.class);
        when(id.value()).thenReturn(UUID.randomUUID());
        when(user.id()).thenReturn(id);
        return user;
    }
}
