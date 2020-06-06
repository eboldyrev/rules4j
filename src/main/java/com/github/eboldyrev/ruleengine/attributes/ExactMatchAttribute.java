package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.AttributeDefinition;

public class ExactMatchAttribute extends BasicRuleAttribute {

    ExactMatchAttribute(AttributeDefinition attributeDefinition, String value) {
        super(attributeDefinition, value);
    }

    @Override
    public boolean calculate(RuleAttribute other){
        return this.equals(other);
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, EXACT_MATCH_COEFFICIENT + definition.getWeight());
    }

    @Override
    public String toString() {
        return "ExactMatchAttribute{" +
                "attributeDefinition='" + definition + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
