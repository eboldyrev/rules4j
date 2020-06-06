package com.github.eboldyrev.ruleengine.attributes;

import com.github.eboldyrev.ruleengine.AttributeDefinition;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RuleAttribute {
    int EXACT_MATCH_COEFFICIENT = 3;
    int PARTIAL_MATCH_COEFFICIENT = 1;
    int ANY_MATCH_COEFFICIENT = 0;

    String divider = ":";
    char anyValue = '*';
    String anyCharsValue = "*";
    int weightBase = 2;

    boolean calculate(RuleAttribute ruleAttribute);

    AttributeDefinition getDefinition();

    String asString();

    String getName();

    String getValue();

    int getWeight();

    static RuleAttribute fromString(String ruleAttr,
                                    Map<String, AttributeDefinition> attributeDefinitions,
                                    Function<String, String> nameTransformator,
                                    Function<String, String> valueTransformator,
                                    Function<String, AttributeDefinition> unknownAttributePolicy,
                                    Consumer<RuleAttribute> validateAttributeType){
        int idx = validateRuleAttribute(ruleAttr);

        AttributeDefinition attributeDefinition = validateAndGetName(ruleAttr, attributeDefinitions, nameTransformator,
                idx, unknownAttributePolicy);

        if (attributeDefinition != null) {
            String value = validateAndGetValue(ruleAttr, valueTransformator, idx);

            RuleAttribute result;
            if (value.length() == 1 && value.charAt(0) == anyValue) {
                result = new AnyRuleAttribute(attributeDefinition, value);
            } else if (value.endsWith(anyCharsValue)) {
                result = new StartsWithRuleAttribute(attributeDefinition, value.substring(0, value.length() - 1));
            } else if (value.startsWith(anyCharsValue)) {
                result = new EndsWithRuleAttribute(attributeDefinition, value.substring(1));
            } else {
                result = new ExactMatchAttribute(attributeDefinition, value);
            }
            if (validateAttributeType != null) {
                validateAttributeType.accept(result);
            }

            return result;
        }

        return null;
    }

    static String validateAndGetValue(String ruleAttr, Function<String, String> valueTransformator, int idx) {
        String value = ruleAttr.substring(idx+1).trim();
        if (value.length() == 0) {
            throw new InvalidRuleStructure("Empty value in '" + ruleAttr+"'");
        }
        if (valueTransformator != null) {
            value = valueTransformator.apply(value);
        }
        return value;
    }

    static AttributeDefinition validateAndGetName(String ruleAttr,
                                                  Map<String, AttributeDefinition> attributeDefinitions,
                                                  Function<String, String> nameTransformator,
                                                  int idx,
                                                  Function<String, AttributeDefinition> unknownAttributePolicy) {
        String name = ruleAttr.substring(0, idx).trim();
        if (name.length() == 0) {
            throw new InvalidRuleStructure("Empty name in '" + ruleAttr+"'");
        }
        if (nameTransformator != null) {
            name = nameTransformator.apply(name);
        }

        AttributeDefinition attributeDefinition = attributeDefinitions.get(name);
        if (attributeDefinition ==  null) {
            attributeDefinition = unknownAttributePolicy.apply(name);
        }
        return attributeDefinition;
    }

    static int validateRuleAttribute(String ruleAttr) {
        int idx = ruleAttr.indexOf(divider);
        if (idx < 0) {
            throw new InvalidRuleStructure("Can't parse "+ ruleAttr+". Missing '"+divider+"'.");
        }
        return idx;
    }

}
