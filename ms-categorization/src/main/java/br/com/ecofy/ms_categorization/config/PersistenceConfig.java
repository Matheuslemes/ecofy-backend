package br.com.ecofy.ms_categorization.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.ecofy.categorization.adapters.out.persistence.repository")
public class PersistenceConfig { }