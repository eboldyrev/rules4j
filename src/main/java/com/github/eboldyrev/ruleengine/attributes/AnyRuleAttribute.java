package com.github.eboldyrev.ruleengine.attributes;

public class AnyRuleAttribute extends BasicRuleAttribute {

    // for now we can leave it as a string inside - without parsing into name and value
    public AnyRuleAttribute(String name, String value, String ruleAttribute) {
        super(name, value, ruleAttribute);
    }

    @Override
    public boolean calculate(RuleAttribute other){
        return this.name.equals(other.getName());
    }

    @Override
    public int getWeight() {
        return (int) Math.pow(weightBase, 0);
    }

    @Override
    public String toString() {
        return "AnyRuleAttribute{" +
                "ruleAttribute='" + attributeDefinition + '\'' +
                '}';
    }
}
