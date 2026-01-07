package br.com.ecofy.ms_notification.config;

import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    /**
     * Garante UUID em padrão consistente (STANDARD) para interoperabilidade.
     */
    @Bean
    MongoClientSettingsBuilderCustomizer uuidRepresentationCustomizer() {
        return (MongoClientSettings.Builder builder) ->
                builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }
}

