package br.com.ecofy.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MailConfig.EcofyMailProperties.class)
public class MailConfig {

    // apenas expoe as propriedades como bean
    // o MailProviderAdapter pode receber EcofyMailProperties no construtor.
    public static class MailConfigException extends RuntimeException {
        public MailConfigException(String message) {
            super(message);
        }
    }

    @ConfigurationProperties(prefix = "ecofy.mail")
    public static class EcofyMailProperties {

        // endereco padrão de remetente
        // ex: no-reply@ecofy.com
        private String from = "no-reply@ecofy.com";


        // url base do frontend para montar links de confirmação / reset.
        // ex.: https://app.ecofy.com
        private String frontendBaseUrl = "https://app.ecofy.com";

        // caminho base de templates de e-mail (se você quiser usar FreeMarker, Thymeleaf, etc.).
        private String templatesBasePath = "classpath:/mail-templates";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFrontendBaseUrl() {
            return frontendBaseUrl;
        }

        public void setFrontendBaseUrl(String frontendBaseUrl) {
            this.frontendBaseUrl = frontendBaseUrl;
        }

        public String getTemplatesBasePath() {
            return templatesBasePath;
        }

        public void setTemplatesBasePath(String templatesBasePath) {
            this.templatesBasePath = templatesBasePath;
        }

    }

}
