package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;

import java.util.Objects;

import static com.github.eboldyrev.ruleengine.attributes.RuleAttribute.EXACT_MATCH_COEFFICIENT;

public class AttributeDefinition {
    public static final int MAX_EXPONENT_FOR_ATTRIBUTE_WEIGHT = 31 - EXACT_MATCH_COEFFICIENT;

    private final String name;
    private final int weight;

    AttributeDefinition(String name, int weight) {
        validateAttributeName(name);
        validateWeight(weight);

        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    private void validateWeight(Integer weight) {
        if ( weight < 0 ) {
            throw new InvalidRuleStructure("Attribute weight can't be negative.");
        } else if (weight > MAX_EXPONENT_FOR_ATTRIBUTE_WEIGHT) {
            throw new InvalidRuleStructure("Attribute weight can't be greater " + MAX_EXPONENT_FOR_ATTRIBUTE_WEIGHT);
        }
    }

    private void validateAttributeName(String attrName) {
        if (attrName.trim().length() == 0) {
            throw new InvalidRuleStructure("Attribute name can't be an empty string.");
        }

        if (attrName.contains(Rule.equalityDivider)) {
            throw new InvalidRuleStructure("Attribute name contains invalid symbol "+Rule.equalityDivider);
        } else if (attrName.contains(Rule.divider)) {
            throw new InvalidRuleStructure("Attribute name contains invalid symbol "+Rule.divider);
        } else if (attrName.contains(RuleAttribute.divider)) {
            throw new InvalidRuleStructure("Attribute name contains invalid symbol "+RuleAttribute.divider);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeDefinition that = (AttributeDefinition) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
