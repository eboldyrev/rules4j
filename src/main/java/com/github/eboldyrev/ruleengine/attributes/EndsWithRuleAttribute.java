package com.github.eboldyrev.ruleengine.attributes;

public class EndsWithRuleAttribute extends BasicRuleAttribute {

    public EndsWithRuleAttribute(AttributeDefinition attributeDefinition, String value) {
        super(attributeDefinition, value);
    }

    @Override
    public String asString() {
        return definition.getName() + divider + anyCharsValue + value;
    }

    @Override
    public boolean calculate(RuleAttribute other) {
        return this.definition.getName().equals(other.getName()) && other.getValue().endsWith(this.value);
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, PARTIAL_MATCH_COEFFICIENT + definition.getWeight());
    }

    @Override
    public String toString() {
        return "EndsWithRuleAttribute{" +
                "attributeDefinition='" + definition + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
