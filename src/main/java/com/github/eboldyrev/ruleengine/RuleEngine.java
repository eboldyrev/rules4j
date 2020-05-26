package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class RuleEngine {

    private final AtomicReference<Metadata> metadataRef = new AtomicReference<>(Metadata.EMPTY_METADATA);
    private final Function<String, String> nameTransformator;
    private final Function<String, String> valueTransformator;

    public RuleEngine(Function<String, String> attributeNameTransformator,
                      Function<String, String> attributeValueTransformator) {
        this.nameTransformator = attributeNameTransformator;
        this.valueTransformator = attributeValueTransformator;
    }

    public Map<String, AttributeDefinition> createAttributeDefinitions(Map<String, Integer> attrDefs) {
        Map<String, AttributeDefinition> attributeDefinitions = new HashMap<>((int) (attrDefs.size() / 0.75));
        for (Map.Entry<String, Integer> definition : attrDefs.entrySet()) {
            String key = nameTransformator != null ? nameTransformator.apply(definition.getKey()) : definition.getKey();
            attributeDefinitions.put(key, new AttributeDefinition(key, definition.getValue()));
        }
        return attributeDefinitions;
    }

    public Rule parseRule(String id, String ruleStr) throws InvalidRuleStructure {
        return Rule.ruleFromString(id, ruleStr, metadataRef.get().attributeDefinitions, nameTransformator, valueTransformator);
    }

    // TODO support Collection as argument ??
    // TODO return Set ??
    public List<Rule> parseRules(Map<String, String> idRuleMap) throws InvalidRuleStructure {
        List<Rule> rules = new ArrayList<>(idRuleMap.size());
        for (Map.Entry<String, String> entry : idRuleMap.entrySet()) {
            Rule rule = parseRule(entry.getKey(), entry.getValue());
            rules.add(rule);
        }
        return rules;
    }

    public void setRules(Map<String, String> idRuleMap) {
        requireNonNull(idRuleMap);
        List<Rule> rules = parseRules(idRuleMap);
        metadataRef.set(new Metadata(metadataRef.get().attributeDefinitions, rules));
    }

    public void setAttributesDefinitions(Map<String, Integer> attrDefs) {
        requireNonNull(attrDefs);
        Map<String, AttributeDefinition> attributeDefinitions = createAttributeDefinitions(attrDefs);
        metadataRef.set(new Metadata(attributeDefinitions, metadataRef.get().rules));
    }

    public void setRulesAndAttributeDefinitions(Map<String, Integer> attrDefs, Map<String, String> idRuleMap) {
        setAttributesDefinitions(attrDefs);
        setRules(idRuleMap);
    }

    public List<Rule> getRules() {
        return Collections.unmodifiableList(metadataRef.get().rules);
    }

    public Map<String, Integer> getAttributesDefinitions() {
        return metadataRef.get().attributeDefinitions.entrySet()
                .stream()
                .collect(Collectors.toMap(e-> e.getKey(), e-> e.getValue().getWeight()));
    }

    public Map<String, String> getRulesAsStrings() {
        List<Rule> rules = metadataRef.get().getRules();
        Map<String, String> result = new HashMap<>((int) (rules.size() / 0.75));
        for (Rule rule : rules) {
            result.put(rule.getId(), rule.asString());
        }
        return result;
    }

    public String query(Map<String, String> queryAttrs) throws InvalidRuleStructure {
        List<RuleAttribute> queryAttributes = Rule.queryFromMap(queryAttrs, metadataRef.get().attributeDefinitions, nameTransformator, valueTransformator);
        return query(queryAttributes);
    }

    public String query(String queryAttrsStr) throws InvalidRuleStructure {
        List<RuleAttribute> queryAttributes = Rule.queryFromString(queryAttrsStr, metadataRef.get().attributeDefinitions, nameTransformator, valueTransformator);
        return query(queryAttributes);
    }

    private String query(List<RuleAttribute> queryAttributes) {
        List<RuleResult> possibleResults = new ArrayList<>();
        RuleResult notEqualResult = RuleResult.notEqual(null);
        possibleResults.add(notEqualResult);
        List<Rule> currentRules = metadataRef.get().rules;
        for (Rule rule : currentRules) {
            RuleResult ruleResult = rule.execute(queryAttributes);
            if (ruleResult.getStatus() == RuleResult.Status.EQUAL) {
                if (possibleResults.get(0).getRuleWeight() <= ruleResult.getRuleWeight()) {
                    possibleResults.add(0, ruleResult);
                }
            }
        }

        if (possibleResults.size() > 1) {
            if (possibleResults.get(0).getRuleWeight() == possibleResults.get(1).getRuleWeight()) {
                List<RuleResult> multiplyRulesFound = possibleResults.stream()
                        .filter(ruleResult -> ruleResult.getRuleWeight() == possibleResults.get(0).getRuleWeight())
                        .collect(Collectors.toList());
                throw new MultiplyRulesFound("Multiply rules found.", multiplyRulesFound);
            }
        } else if (notEqualResult.equals(possibleResults.get(0))) {
            return null;
        }

        return possibleResults.get(0).getResultValue();
    }

    public Function<String, String> getNameTransformator() {
        return nameTransformator;
    }

    public Function<String, String> getValueTransformator() {
        return valueTransformator;
    }

    static class Metadata {
        static final Metadata EMPTY_METADATA = new Metadata(emptyMap(), emptyList());

        private final Map<String, AttributeDefinition> attributeDefinitions;
        private final List<Rule> rules;

        Metadata(Map<String, AttributeDefinition> attributeDefinitions, List<Rule> rules) {
            this.attributeDefinitions = attributeDefinitions;
            this.rules = rules;
        }

        Map<String, AttributeDefinition> getAttributeDefinitions() {
            return attributeDefinitions;
        }

        List<Rule> getRules() {
            return rules;
        }
    }

}
