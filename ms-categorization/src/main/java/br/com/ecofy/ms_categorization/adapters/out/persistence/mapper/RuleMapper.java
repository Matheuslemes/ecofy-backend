package br.com.ecofy.ms_categorization.adapters.out.persistence.mapper;

import br.com.ecofy.ms_categorization.adapters.out.persistence.entity.CategorizationRuleEntity;
import br.com.ecofy.ms_categorization.core.domain.CategorizationRule;
import br.com.ecofy.ms_categorization.core.domain.valueobject.RuleCondition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class RuleMapper {

    private static final TypeReference<List<RuleCondition>> CONDITIONS_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RuleMapper() {
        this(new ObjectMapper(), Clock.systemUTC());
    }

    public RuleMapper(ObjectMapper objectMapper) {
        this(objectMapper, Clock.systemUTC());
    }

    public RuleMapper(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public CategorizationRule toDomain(CategorizationRuleEntity e) {
        Objects.requireNonNull(e, "entity must not be null");

        return new CategorizationRule(
                e.getId(),
                e.getCategoryId(),
                e.getName(),
                e.getStatus(),
                e.getPriority(),
                readConditions(e.getConditionsJson()),
                nonNullOrNow(e.getCreatedAt()),
                nonNullOrNow(e.getUpdatedAt())
        );
    }

    public CategorizationRuleEntity toEntity(CategorizationRule d) {
        Objects.requireNonNull(d, "domain must not be null");

        return CategorizationRuleEntity.builder()
                .id(d.getId())
                .categoryId(d.getCategoryId())
                .name(d.getName())
                .status(d.getStatus())
                .priority(d.getPriority())
                .conditionsJson(writeConditions(d.getConditions()))
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private List<RuleCondition> readConditions(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, CONDITIONS_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize rule conditions JSON", ex);
        }
    }

    private String writeConditions(List<RuleCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(conditions);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize rule conditions to JSON", ex);
        }
    }

    private Instant nonNullOrNow(Instant value) {
        return value != null ? value : Instant.now(clock);
    }
}
