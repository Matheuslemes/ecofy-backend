package br.com.ecofy.auth.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailConfigTest {

    @Test
    void ecofyMailProperties_shouldHaveDefaults_andAllowOverrides() {
        MailConfig.EcofyMailProperties p = new MailConfig.EcofyMailProperties();

        assertEquals("no-reply@ecofy.com", p.getFrom());
        assertEquals("https://app.ecofy.com", p.getFrontendBaseUrl());
        assertEquals("classpath:/mail-templates", p.getTemplatesBasePath());

        p.setFrom("support@ecofy.com");
        p.setFrontendBaseUrl("https://frontend.test");
        p.setTemplatesBasePath("classpath:/templates");

        assertEquals("support@ecofy.com", p.getFrom());
        assertEquals("https://frontend.test", p.getFrontendBaseUrl());
        assertEquals("classpath:/templates", p.getTemplatesBasePath());
    }

    @Test
    void mailConfigException_shouldStoreMessage() {
        MailConfig.MailConfigException ex = new MailConfig.MailConfigException("x");
        assertEquals("x", ex.getMessage());
    }
}
