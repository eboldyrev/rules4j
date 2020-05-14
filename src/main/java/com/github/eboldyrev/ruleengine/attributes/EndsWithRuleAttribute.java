package com.github.eboldyrev.ruleengine.attributes;

public class EndsWithRuleAttribute extends BasicRuleAttribute {

    public EndsWithRuleAttribute(String name, String value, String ruleAttribute) {
        super(name, value, ruleAttribute);
    }

    @Override
    public String asString() {
        return name + divider + anyCharsValue + value;
    }

    @Override
    public boolean calculate(RuleAttribute other){
        return this.name.equals(other.getName()) && other.getValue().endsWith(this.value);
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, 1);
    }

    @Override
    public String toString() {
        return "EndsWithRuleAttribute{" +
                "attributeDefinition='" + attributeDefinition + '\'' +
                '}';
    }
}
