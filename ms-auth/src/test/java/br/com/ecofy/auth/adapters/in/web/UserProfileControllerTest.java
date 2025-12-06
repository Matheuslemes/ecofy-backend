package br.com.ecofy.auth.adapters.in.web;

import br.com.ecofy.auth.adapters.in.web.dto.response.UserResponse;
import br.com.ecofy.auth.core.domain.AuthUser;
import br.com.ecofy.auth.core.port.in.GetCurrentUserProfileUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

    @Mock
    private GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;

    @Test
    void me_shouldReturnUserProfileMappedSuccessfully() {

        // Arrange
        UserProfileController controller = new UserProfileController(getCurrentUserProfileUseCase);

        AuthUser domainUser = mock(AuthUser.class, Answers.RETURNS_DEEP_STUBS);

        UUID id = UUID.randomUUID();

        when(domainUser.id().value()).thenReturn(id);
        when(domainUser.email().value()).thenReturn("user@example.com");
        when(domainUser.fullName()).thenReturn("John Doe");
        when(domainUser.status().name()).thenReturn("ACTIVE");
        when(domainUser.isEmailVerified()).thenReturn(true);
        when(domainUser.roles()).thenReturn(Set.of());
        when(domainUser.directPermissions()).thenReturn(Set.of());

        when(getCurrentUserProfileUseCase.getCurrentUser()).thenReturn(domainUser);

        // Act
        ResponseEntity<UserResponse> response = controller.me();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());


        UserResponse body = response.getBody();
        assertNotNull(body);

        assertEquals(id.toString(), body.id());
        assertEquals("user@example.com", body.email());
        assertEquals("John Doe", body.fullName());
        assertEquals("ACTIVE", body.status());
        assertTrue(body.emailVerified());

        // Nenhuma role ou permission
        assertTrue(body.roles().isEmpty());
        assertTrue(body.permissions().isEmpty());

        // Chamou o caso de uso exatamente 1x
        verify(getCurrentUserProfileUseCase, times(1)).getCurrentUser();
    }

    @Test
    void me_shouldThrowNullPointer_ifUserReturnsNull() {
        // Arrange
        UserProfileController controller = new UserProfileController(getCurrentUserProfileUseCase);

        when(getCurrentUserProfileUseCase.getCurrentUser()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, controller::me);

        verify(getCurrentUserProfileUseCase).getCurrentUser();
    }
}
