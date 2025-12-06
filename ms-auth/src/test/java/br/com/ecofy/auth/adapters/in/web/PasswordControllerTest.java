package br.com.ecofy.auth.adapters.in.web;

import br.com.ecofy.auth.adapters.in.web.dto.request.PasswordResetConfirmRequest;
import br.com.ecofy.auth.adapters.in.web.dto.request.PasswordResetRequest;
import br.com.ecofy.auth.core.port.in.RequestPasswordResetUseCase;
import br.com.ecofy.auth.core.port.in.ResetPasswordUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    private RequestPasswordResetUseCase requestPasswordResetUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    private PasswordController controller;

    @BeforeEach
    void setUp() {
        controller = new PasswordController(requestPasswordResetUseCase, resetPasswordUseCase);
    }

    @Test
    void requestReset_shouldReturnAcceptedAndCallUseCaseWithCorrectEmail() {

        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("user@example.com");

        // Act
        ResponseEntity<Void> response = controller.requestReset(request);

        // Assert status
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        // sem body
        assertEquals(null, response.getBody());

        // Captura comando enviado ao use case
        ArgumentCaptor<RequestPasswordResetUseCase.RequestPasswordResetCommand> captor =
                ArgumentCaptor.forClass(RequestPasswordResetUseCase.RequestPasswordResetCommand.class);

        verify(requestPasswordResetUseCase, times(1)).requestReset(captor.capture());
        verifyNoMoreInteractions(requestPasswordResetUseCase, resetPasswordUseCase);

        RequestPasswordResetUseCase.RequestPasswordResetCommand cmd = captor.getValue();
        assertNotNull(cmd);
        assertEquals("user@example.com", cmd.email());

    }

    @Test
    void confirmReset_shouldReturnNoContentAndCallUseCaseWithCorrectTokenAndPassword() {

        // Arrange
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "reset-token-123",
                "NewStrongPass!23"
        );

        // Act
        ResponseEntity<Void> response = controller.confirmReset(request);

        // Assert status
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(null, response.getBody());

        // Captura comando enviado ao use case
        ArgumentCaptor<ResetPasswordUseCase.ResetPasswordCommand> captor =
                ArgumentCaptor.forClass(ResetPasswordUseCase.ResetPasswordCommand.class);

        verify(resetPasswordUseCase, times(1)).resetPassword(captor.capture());
        verifyNoMoreInteractions(resetPasswordUseCase, requestPasswordResetUseCase);

        ResetPasswordUseCase.ResetPasswordCommand cmd = captor.getValue();
        assertNotNull(cmd);
        assertEquals("reset-token-123", cmd.resetToken());
        assertEquals("NewStrongPass!23", cmd.newPassword());

    }

}
