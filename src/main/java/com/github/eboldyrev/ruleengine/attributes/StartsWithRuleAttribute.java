package com.github.eboldyrev.ruleengine.attributes;

public class StartsWithRuleAttribute extends BasicRuleAttribute {

    public StartsWithRuleAttribute(String name, String value, String ruleAttribute) {
        super(name, value, ruleAttribute);
    }

    @Override
    public String asString() {
        return name + divider + value + anyCharsValue;
    }

    @Override
    public boolean calculate(RuleAttribute other){
        return this.name.equals(other.getName()) && other.getValue().startsWith(this.value);
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, 1);
    }

    @Override
    public String toString() {
        return "StartsWithRuleAttribute{" +
                "ruleAttribute='" + attributeDefinition + '\'' +
                '}';
    }
}
