package br.com.ecofy.auth.adapters.in.web;

import br.com.ecofy.auth.core.port.in.GetJwksUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwksControllerTest {

    @Mock
    private GetJwksUseCase getJwksUseCase;

    private JwksController controller;

    @BeforeEach
    void setUp() {
        controller = new JwksController(getJwksUseCase);
    }

    private void assertJwksCacheHeaders(HttpHeaders headers) {
        String cacheControl = headers.getCacheControl();
        assertNotNull(cacheControl, "Cache-Control deve estar definido");

        assertTrue(cacheControl.toLowerCase().contains("max-age"),
                "Cache-Control deve conter max-age");
    }

    @Test
    void jwks_shouldReturnOkWithBodyAndCacheHeaders_whenKeysIsCollection() {

        // Arrange
        Map<String, Object> jwks = Map.of(
                "keys", List.of(
                        Map.of("kty", "RSA", "kid", "key-1"),
                        Map.of("kty", "RSA", "kid", "key-2")
                )
        );

        when(getJwksUseCase.getJwks()).thenReturn(jwks);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.jwks();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertSame(jwks, response.getBody(), "Body deve ser exatamente o mapa retornado pelo use case");

        assertJwksCacheHeaders(response.getHeaders());

    }

    @Test
    void jwks_shouldReturnOkWithBodyAndCacheHeaders_whenKeysIsNotCollection() {

        Map<String, Object> jwks = Map.of(
                "keys", "not-a-collection"
        );

        when(getJwksUseCase.getJwks()).thenReturn(jwks);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.jwks();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertSame(jwks, response.getBody());

        assertJwksCacheHeaders(response.getHeaders());

    }
}
