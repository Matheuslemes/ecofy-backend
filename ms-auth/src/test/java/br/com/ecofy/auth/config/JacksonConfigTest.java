package br.com.ecofy.auth.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {

    @Test
    void objectMapper_shouldDisableExpectedFeatures_andSupportJavaTimeSerialization() throws JsonProcessingException {
        JacksonConfig config = new JacksonConfig();

        ObjectMapper mapper = config.objectMapper();

        assertNotNull(mapper);

        assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        String json = mapper.writeValueAsString(new Dto(Instant.parse("2024-01-01T00:00:00Z")));
        assertEquals("{\"at\":\"2024-01-01T00:00:00Z\"}", json);
    }

    private record Dto(Instant at) { }
}
