package br.com.ecofy.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    @Test
    void authEventProducerFactory_shouldConfigureBootstrapAndKeySerializer_andUseJsonSerializerWithTypeInfoDisabled() throws Exception {
        KafkaConfig config = new KafkaConfig();

        String bootstrap = "localhost:9092";
        ObjectMapper objectMapper = new ObjectMapper();

        ProducerFactory<String, Object> pf = config.authEventProducerFactory(bootstrap, objectMapper);

        assertNotNull(pf);
        assertInstanceOf(DefaultKafkaProducerFactory.class, pf);

        DefaultKafkaProducerFactory<?, ?> dpf = (DefaultKafkaProducerFactory<?, ?>) pf;

        Map<String, Object> props = dpf.getConfigurationProperties();
        assertEquals(bootstrap, props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));

        Serializer<?> valueSerializer = extractValueSerializer(dpf);
        assertNotNull(valueSerializer);

        assertEquals(
                "org.springframework.kafka.support.serializer.JsonSerializer",
                valueSerializer.getClass().getName()
        );

        Boolean addTypeInfo = readBooleanGetterIfExists(valueSerializer, "isAddTypeInfo");
        if (addTypeInfo == null) addTypeInfo = readBooleanGetterIfExists(valueSerializer, "isAddTypeHeaders");
        if (addTypeInfo == null) addTypeInfo = readBooleanGetterIfExists(valueSerializer, "isAddTypeInformation");

        Boolean addTypeInfoField = readBooleanFieldViaGetterFallback(valueSerializer, "addTypeInfo");
        if (addTypeInfo == null) addTypeInfo = addTypeInfoField;

        assertNotNull(addTypeInfo);
        assertFalse(addTypeInfo);
    }

    @Test
    void authEventKafkaTemplate_shouldSetDefaultTopic() {
        KafkaConfig config = new KafkaConfig();

        ProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(Map.of());

        KafkaTemplate<String, Object> template = config.authEventKafkaTemplate(pf);

        assertNotNull(template);
        assertEquals("auth.events", template.getDefaultTopic());
    }

    private static Serializer<?> extractValueSerializer(DefaultKafkaProducerFactory<?, ?> dpf) throws Exception {
        for (String name : new String[]{"getValueSerializer", "valueSerializer"}) {
            try {
                Method m = dpf.getClass().getMethod(name);
                Object v = m.invoke(dpf);
                if (v instanceof Serializer<?> s) return s;
            } catch (NoSuchMethodException ignored) {
            }
        }

        for (Method m : dpf.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && Serializer.class.isAssignableFrom(m.getReturnType())) {
                Object v = m.invoke(dpf);
                if (v instanceof Serializer<?> s) return s;
            }
        }

        throw new AssertionError("Could not extract value serializer from " + dpf.getClass().getName());
    }

    private static Boolean readBooleanGetterIfExists(Object target, String methodName) {
        try {
            Object v = target.getClass().getMethod(methodName).invoke(target);
            return (v instanceof Boolean b) ? b : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Boolean readBooleanFieldViaGetterFallback(Object target, String logicalName) {
        String cap = Character.toUpperCase(logicalName.charAt(0)) + logicalName.substring(1);
        for (String getter : new String[]{"get" + cap, "is" + cap}) {
            Boolean v = readBooleanGetterIfExists(target, getter);
            if (v != null) return v;
        }
        return null;
    }
}
