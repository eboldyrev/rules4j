package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.AttributeDefinition;

import java.util.Objects;

public abstract class BasicRuleAttribute implements RuleAttribute {

    protected final String value;
    protected final AttributeDefinition definition;

    BasicRuleAttribute(AttributeDefinition attributeDefinition, String value) {
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
    public abstract int getWeight();

    @Override
    public abstract boolean calculate(RuleAttribute other);

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
