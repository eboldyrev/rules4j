package com.github.eboldyrev.ruleengine.attributes;

public class AnyRuleAttribute extends BasicRuleAttribute {

    // for now we can leave it as a string inside - without parsing into name and value
    public AnyRuleAttribute(AttributeDefinition attributeDefinition, String value) {
        super(attributeDefinition, value);
    }

    @Override
    public boolean calculate(RuleAttribute other){
        return this.definition.getName().equals(other.getName());
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, ANY_MATCH_COEFFICIENT);
    }

    @Override
    public String toString() {
        return "AnyRuleAttribute{" +
                "attributeDefinition='" + definition + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
