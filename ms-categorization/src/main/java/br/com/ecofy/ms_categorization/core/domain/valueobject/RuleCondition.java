package br.com.ecofy.ms_categorization.core.domain.valueobject;


import br.com.ecofy.ms_categorization.core.domain.enums.MatchOperator;

import java.util.Objects;

public final class RuleCondition {

    private final String field;
    private final MatchOperator operator;
    private final String value;
    private final Integer weight;

    public RuleCondition(String field, MatchOperator operator, String value, Integer weight) {
        this.field = Objects.requireNonNull(field, "field must not be null");
        this.operator = Objects.requireNonNull(operator, "operator must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
        this.weight = (weight == null) ? 1 : weight;
    }

    public String getField() {
        return field;
    }

    public MatchOperator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public Integer getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleCondition that)) return false;
        return field.equals(that.field) &&
                operator == that.operator &&
                value.equals(that.value) &&
                weight.equals(that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, operator, value, weight);
    }

    @Override
    public String toString() {
        return "RuleCondition[" +
                "field=" + field +
                ", operator=" + operator +
                ", value=" + value +
                ", weight=" + weight +
                ']';
    }

}