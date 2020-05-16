package com.github.eboldyrev.ruleengine.attributes;

import java.util.Objects;

public class BasicRuleAttribute implements RuleAttribute {

    protected final String value;
    protected final AttributeDefinition definition;

    public BasicRuleAttribute(AttributeDefinition attributeDefinition, String value) {
        this.definition = attributeDefinition;
        this.value = value;
    }

    @Override
    public AttributeDefinition getDefinition() {
        return definition;
    }

    @Override
    public String asString() {
        return definition.getName() + RuleAttribute.divider + value;
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, EXACT_MATCH_COEFFICIENT + definition.getWeight());
    }

    @Override
    public boolean calculate(RuleAttribute other) {
        return this.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleAttribute)) return false;
        RuleAttribute that = (RuleAttribute) o;
        return definition.equals(that.getDefinition()) && value.equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition, value);
    }

    @Override
    public String toString() {
        return "name=" + definition.getName() + " , value=" + value;
    }
}
