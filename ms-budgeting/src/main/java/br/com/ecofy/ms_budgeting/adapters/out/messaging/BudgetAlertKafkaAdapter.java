package br.com.ecofy.ms_budgeting.adapters.out.messaging;

import br.com.ecofy.ms_budgeting.adapters.out.messaging.mapper.EventMapper;
import br.com.ecofy.ms_budgeting.config.BudgetingProperties;
import br.com.ecofy.ms_budgeting.core.domain.BudgetAlert;
import br.com.ecofy.ms_budgeting.core.port.out.PublishBudgetAlertEventPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class BudgetAlertKafkaAdapter implements PublishBudgetAlertEventPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BudgetingProperties props;

    public BudgetAlertKafkaAdapter(KafkaTemplate<String, Object> kafkaTemplate, BudgetingProperties props) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public void publish(BudgetAlert alert) {
        var event = EventMapper.toEvent(alert);
        kafkaTemplate.send(props.topics().budgetAlert(), alert.getBudgetId().toString(), event);
        log.info("[BudgetAlertKafkaAdapter] - [publish] -> topic={} budgetId={} severity={}",
                props.topics().budgetAlert(), alert.getBudgetId(), alert.getSeverity());
    }

}