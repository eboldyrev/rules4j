package com.github.eboldyrev.ruleengine.attributes;

import java.util.Objects;

public class BasicRuleAttribute implements RuleAttribute {
    protected String name;
    protected String value;
    protected String attributeDefinition;

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
    public boolean calculate(RuleAttribute other){
        return this.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleAttribute)) return false;
        RuleAttribute that = (RuleAttribute) o;
        return attributeDefinition.equals(that.getAttributeDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeDefinition);
    }

    @Override
    public String toString() {
        return "BasicRuleAttribute{" +
               "attributeString='" + attributeDefinition +"'" +
                '}';
    }
}
