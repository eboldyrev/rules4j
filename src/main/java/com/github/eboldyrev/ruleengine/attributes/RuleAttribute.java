package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;

import java.util.function.Function;

public interface RuleAttribute {
    char divider = ':';
    char anyValue = '*';
    String anyCharsValue = "*";
    int weightBase = 2;

    boolean calculate(RuleAttribute ruleAttribute);

    String getAttributeDefinition();

    String asString();

    String getName();

    String getValue();

    int getWeight();

    // BrandId:10
    static RuleAttribute fromString(String ruleAttr, Function<String, String> valueTransformator){
        int idx = ruleAttr.indexOf(divider);
        if (idx < 0) {
            throw new InvalidRuleStructure("Invalid rule attribute: " + ruleAttr);
        }

        String name = ruleAttr.substring(0, idx);
        String value = ruleAttr.substring(idx+1);
        if (valueTransformator != null) {
            value = valueTransformator.apply(value);
        }

        if (name.length() == 0 || value.length() == 0) {
            throw new InvalidRuleStructure("Empty name/value: " + ruleAttr);
        }

        if (value.length() == 1 && value.charAt(0) == anyValue) {
            return new AnyRuleAttribute(name, value, ruleAttr);
        }

        if (value.endsWith(anyCharsValue)) {
            return new StartsWithRuleAttribute(name, value.substring(0, value.length()-1), ruleAttr);
        }

        if (value.startsWith(anyCharsValue)) {
            return new EndsWithRuleAttribute(name, value.substring(1), ruleAttr);
        }

        return new BasicRuleAttribute(name, value, ruleAttr);
    }

}
