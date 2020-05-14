package com.github.eboldyrev.ruleengine.attributes;

import java.util.Objects;

public class BasicRuleAttribute implements RuleAttribute {
    protected final String name;
    protected final String value;
    protected final String attributeDefinition;

    public BasicRuleAttribute(String name, String value, String attributeDefinition) {
        this.name = name;
        this.value = value;
        this.attributeDefinition = attributeDefinition;
    }

    @Override
    public String getAttributeDefinition() {
        return attributeDefinition;
    }

    @Override
    public String asString() {
        return name + RuleAttribute.divider + value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, 3);
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
        return name.equals(that.getName()) && value.equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "name=" + name + " , value=" + value;
    }
}
