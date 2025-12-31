package br.com.ecofy.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void ecofyAuthOpenAPI_shouldConfigureInfoExternalDocsSecuritySchemeAndRequirement() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI api = config.ecofyAuthOpenAPI();

        assertNotNull(api);

        Info info = api.getInfo();
        assertNotNull(info);
        assertEquals("EcoFy Auth Service – OIDC/JWT", info.getTitle());
        assertEquals("v1.0.0", info.getVersion());
        assertNotNull(info.getDescription());
        assertTrue(info.getDescription().contains("Microserviço de autenticação"));

        Contact contact = info.getContact();
        assertNotNull(contact);
        assertEquals("EcoFy Platform", contact.getName());
        assertEquals("dev@ecofy.com", contact.getEmail());
        assertEquals("https://ecofy.com", contact.getUrl());

        License license = info.getLicense();
        assertNotNull(license);
        assertEquals("Proprietary / EcoFy", license.getName());
        assertEquals("https://ecofy.com/license", license.getUrl());

        assertEquals("https://ecofy.com/terms", info.getTermsOfService());

        assertNotNull(api.getExternalDocs());
        assertEquals("EcoFy Platform – Docs", api.getExternalDocs().getDescription());
        assertEquals("https://docs.ecofy.com", api.getExternalDocs().getUrl());

        assertNotNull(api.getComponents());
        assertNotNull(api.getComponents().getSecuritySchemes());
        assertTrue(api.getComponents().getSecuritySchemes().containsKey("BearerAuth"));

        SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("BearerAuth");
        assertNotNull(scheme);
        assertEquals("Authorization", scheme.getName());
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
        assertNotNull(scheme.getDescription());
        assertTrue(scheme.getDescription().contains("Bearer"));

        assertNotNull(api.getSecurity());
        assertFalse(api.getSecurity().isEmpty());
        assertNotNull(api.getSecurity().get(0));
        assertTrue(api.getSecurity().get(0).containsKey("BearerAuth"));
    }

    @Test
    void authApiGroup_shouldMatchApiPathsAndExcludeActuator() throws Exception {
        OpenApiConfig config = new OpenApiConfig();

        GroupedOpenApi group = config.authApiGroup();

        assertNotNull(group);
        assertEquals("auth-api", group.getGroup());

        assertArrayEquals(new String[]{"/api/**"}, readStringArrayField(group, "pathsToMatch"));
        assertArrayEquals(new String[]{"/actuator/**"}, readStringArrayField(group, "pathsToExclude"));
    }

    @Test
    void actuatorGroup_shouldMatchActuatorPaths() throws Exception {
        OpenApiConfig config = new OpenApiConfig();

        GroupedOpenApi group = config.actuatorGroup();

        assertNotNull(group);
        assertEquals("actuator", group.getGroup());

        assertArrayEquals(new String[]{"/actuator/**"}, readStringArrayField(group, "pathsToMatch"));
    }

    private static String[] readStringArrayField(Object target, String fieldName) throws Exception {
        java.lang.reflect.Field f = null;
        Class<?> c = target.getClass();
        while (c != null && f == null) {
            try {
                f = c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        if (f == null) throw new AssertionError("Field not found: " + target.getClass().getName() + "." + fieldName);
        f.setAccessible(true);
        Object v = f.get(target);
        if (v == null) return null;
        if (v instanceof String[] arr) return arr;
        if (v instanceof java.util.List<?> list) return list.stream().map(String::valueOf).toArray(String[]::new);
        throw new AssertionError("Unsupported field type for " + fieldName + ": " + v.getClass().getName());
    }
}
