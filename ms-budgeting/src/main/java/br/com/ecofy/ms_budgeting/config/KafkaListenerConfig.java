package br.com.ecofy.ms_budgeting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Slf4j
@Configuration
public class KafkaListenerConfig {

    @Value("${ecofy.budgeting.kafka.listener.concurrency:3}")
    private int concurrency;

    @Bean(name = "budgetingKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> budgetingKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        configurer.configure(factory, consumerFactory);

        factory.setCommonErrorHandler(kafkaErrorHandler);

        if (concurrency < 1) {
            log.warn("[KafkaListenerConfig] concurrency inválida ({}). Forçando para 1.", concurrency);
            concurrency = 1;
        }
        factory.setConcurrency(concurrency);

        log.info("[KafkaListenerConfig] Listener factory configurada. concurrency={}", concurrency);

        return factory;
    }
}
