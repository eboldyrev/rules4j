package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;

import java.util.Map;
import java.util.function.Function;

public interface RuleAttribute {
    int EXACT_MATCH_COEFFICIENT = 3;
    int PARTIAL_MATCH_COEFFICIENT = 1;
    int ANY_MATCH_COEFFICIENT = 0;

    char divider = ':';
    char anyValue = '*';
    String anyCharsValue = "*";
    int weightBase = 2;

    boolean calculate(RuleAttribute ruleAttribute);

    AttributeDefinition getDefinition();

    String asString();

    String getName();

    String getValue();

    int getWeight();

    static RuleAttribute fromString(String ruleAttr, Map<String, AttributeDefinition> attributeDefinitions, Function<String, String> nameTransformator, Function<String, String> valueTransformator){
        int idx = validateRuleAttribute(ruleAttr);

        AttributeDefinition attributeDefinition = validateAndGetName(ruleAttr, attributeDefinitions, nameTransformator, idx);

        String value = validateAndGetValue(ruleAttr, valueTransformator, idx);

        if (value.length() == 1 && value.charAt(0) == anyValue) {
            return new AnyRuleAttribute(attributeDefinition, value);
        }

        if (value.endsWith(anyCharsValue)) {
            return new StartsWithRuleAttribute(attributeDefinition, value.substring(0, value.length()-1));
        }

        if (value.startsWith(anyCharsValue)) {
            return new EndsWithRuleAttribute(attributeDefinition, value.substring(1));
        }

        return new BasicRuleAttribute(attributeDefinition, value);
    }

    static String validateAndGetValue(String ruleAttr, Function<String, String> valueTransformator, int idx) {
        String value = ruleAttr.substring(idx+1);
        if (value.length() == 0) {
            throw new InvalidRuleStructure("Empty value: " + ruleAttr);
        }
        if (valueTransformator != null) {
            value = valueTransformator.apply(value);
        }
        return value;
    }

    static AttributeDefinition validateAndGetName(String ruleAttr, Map<String, AttributeDefinition> attributeDefinitions, Function<String, String> nameTransformator, int idx) {
        String name = ruleAttr.substring(0, idx);
        if (name.length() == 0) {
            throw new InvalidRuleStructure("Empty name: " + ruleAttr);
        }
        if (nameTransformator != null) {
            name = nameTransformator.apply(name);
        }

        AttributeDefinition attributeDefinition = attributeDefinitions.get(name);
        if (attributeDefinition ==  null) {
            throw new InvalidRuleStructure("Unknown rule attribute: " + name);
        }
        return attributeDefinition;
    }

    static int validateRuleAttribute(String ruleAttr) {
        int idx = ruleAttr.indexOf(divider);
        if (idx < 0) {
            throw new InvalidRuleStructure("Invalid rule attribute: " + ruleAttr);
        }
        return idx;
    }

}
