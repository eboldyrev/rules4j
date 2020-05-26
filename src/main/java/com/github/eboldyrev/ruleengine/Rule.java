package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.naturalOrder;

public class Rule {
    private static final String nameValueDivider = ":";
    private static final String divider = "#";
    private static final String equalityDivider = "=";

    private final String id;
    private final List<RuleAttribute> attributes;
    private final String result;
    private final long weight;

    // todo add rule id -- to easily identify it!
    Rule(String ruleId, List<RuleAttribute> attributes, String result) {
        this.id = ruleId;
        this.attributes = attributes;
        this.weight = attributes.stream().map(RuleAttribute::getWeight).reduce(0, Integer::sum);
        this.result = result;
    }

    static Rule ruleFromString(String id, String rule,
                               Map<String, AttributeDefinition> attributeDefinitions,
                               Function<String, String> nameTransformator,
                               Function<String, String> valueTransformator) {
        String[] ruleAndResult = rule.split(equalityDivider);
        if (ruleAndResult.length != 2) {
            throw new InvalidRuleStructure("No rule result found: " + rule);
        }

        return new Rule(id, ruleAttributesFromString(ruleAndResult[0], attributeDefinitions, nameTransformator, valueTransformator), ruleAndResult[1]);
    }

    // name1:10#name2:1#name3:12345
    static List<RuleAttribute> ruleAttributesFromString(String ruleStr,
                                                        Map<String, AttributeDefinition> attributeDefinitions,
                                                        Function<String, String> nameTransformator,
                                                        Function<String, String> valueTransformator) {
        return queryFromString(ruleStr, attributeDefinitions, nameTransformator, valueTransformator,
                name -> {
                    throw new InvalidRuleStructure("Unknown rule attribute: " + name);
                });
    }

    static List<RuleAttribute> queryFromString(String queryStr,
                                               Map<String, AttributeDefinition> attributeDefinitions,
                                               Function<String, String> nameTransformator,
                                               Function<String, String> valueTransformator) throws InvalidRuleStructure {
        return queryFromString(queryStr, attributeDefinitions, nameTransformator, valueTransformator,
                name -> null);
    }

    private static List<RuleAttribute> queryFromString(String queryStr,
                                                       Map<String, AttributeDefinition> attributeDefinitions,
                                                       Function<String, String> nameTransformator,
                                                       Function<String, String> valueTransformator,
                                                       Function<String, AttributeDefinition> unknownAttributePolicy) {
        String[] splittedRule = queryStr.split(divider);

        if (splittedRule.length == 0) {
            throw new InvalidRuleStructure("No attributes found!");
        }

        Arrays.sort(splittedRule, naturalOrder());

        List<RuleAttribute> attributes = new ArrayList<>(splittedRule.length);
        for (String ruleAttrStr : splittedRule) {
            RuleAttribute ruleAttribute = RuleAttribute.fromString(ruleAttrStr, attributeDefinitions,
                    nameTransformator, valueTransformator, unknownAttributePolicy);
            if (ruleAttribute != null) {
                attributes.add(ruleAttribute);
            }
        }
        return attributes;
    }

    static List<RuleAttribute> queryFromMap(Map<String, String> queryAttrs,
                                            Map<String, AttributeDefinition> attributeDefinitions,
                                            Function<String, String> nameTransformator,
                                            Function<String, String> valueTransformator) throws InvalidRuleStructure {
        String queryStr = queryAttrs.entrySet().stream()
                .map(e -> e.getKey() + nameValueDivider + e.getValue())
                .collect(Collectors.joining(divider));
        return queryFromString(queryStr, attributeDefinitions, nameTransformator, valueTransformator);
    }

    RuleResult execute(List<RuleAttribute> queryAttributes) {
        if (this.attributes.size() > queryAttributes.size()) {
            return RuleResult.notApplicable(this);
        }

        boolean calculationResult = true;
        int queryAttrOffset = 0;
        for (int i = 0; i < attributes.size(); i++) {
            boolean foundFlag = true;
            while (!queryAttributes.get(queryAttrOffset + i).getName().equals(attributes.get(i).getName())) {
                queryAttrOffset++;
                if (queryAttrOffset + i == queryAttributes.size()) {
                    foundFlag = false;
                    break;
                }
            }
            if (foundFlag) {
                calculationResult = calculationResult && attributes.get(i).calculate(queryAttributes.get(queryAttrOffset + i));
            } else {
                calculationResult = false;
            }

            if (calculationResult == false) {
                break;
            }
        }

        if (calculationResult) {
            return new RuleResult(this, RuleResult.Status.EQUAL, weight, result);
        } else {
            return RuleResult.notEqual(this);
        }
    }

    public List<RuleAttribute> getAttributes() {
        return unmodifiableList(attributes);
    }

    public String getResult() {
        return result;
    }

    public String getId() {
        return id;
    }

    public String asString() {
        String[] attributeDefs = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            attributeDefs[i] = attributes.get(i).asString();
        }
        String attributesStr = String.join(divider, attributeDefs);
        return String.join(equalityDivider, attributesStr, result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return attributes.equals(rule.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    @Override
    public String toString() {
        return "Rule{" +
                "attributes=" + attributes +
                ", result='" + result + '\'' +
                ", weight=" + weight +
                '}';
    }
}
