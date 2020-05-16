package com.github.eboldyrev.ruleengine.attributes;

public class StartsWithRuleAttribute extends BasicRuleAttribute {

    public StartsWithRuleAttribute(AttributeDefinition attributeDefinition, String value) {
        super(attributeDefinition, value);
    }

    @Override
    public String asString() {
        return definition.getName() + divider + value + anyCharsValue;
    }

    @Override
    public boolean calculate(RuleAttribute other){
        return this.definition.getName().equals(other.getName()) && other.getValue().startsWith(this.value);
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, PARTIAL_MATCH_COEFFICIENT + definition.getWeight());
    }

    @Override
    public String toString() {
        return "StartsWithRuleAttribute{" +
                "attributeDefinition='" + definition + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
