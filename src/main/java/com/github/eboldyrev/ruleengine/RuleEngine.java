package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;
import com.github.eboldyrev.ruleengine.exception.RuleEngineException;

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
            String attributeName = nameTransformator != null ? nameTransformator.apply(definition.getKey()) : definition.getKey();
            attributeDefinitions.put(attributeName, new AttributeDefinition(attributeName, definition.getValue()));
        }
        return attributeDefinitions;
    }

    // can be not safe to use for mass parsing!!!
    public Rule parseRule(String id, String ruleStr) throws InvalidRuleStructure {
        return parseRule(metadataRef.get().attributeDefinitions, id, ruleStr);
    }

    // TODO support Collection as argument ??
    // TODO return Set ??
    // LATER add policy for batch parsing : throw exception or return invalid and valid rules
    public List<RuleOrError> parseRules(Map<String, String> idRuleMap) throws InvalidRuleStructure {
        Map<String, AttributeDefinition> attributeDefinitions = metadataRef.get().attributeDefinitions;
        return parseRules(attributeDefinitions, idRuleMap);
    }

    // what will happen when setRules and settAttributeDefinitions will be called from different threads
    public void setRules(Map<String, String> idRuleMap) {
        requireNonNull(idRuleMap);
        Map<String, AttributeDefinition> attributeDefinitions = metadataRef.get().attributeDefinitions;
        List<RuleOrError> ruleOrErrors = parseRules(attributeDefinitions, idRuleMap);
        List<Rule> rules = checkForErrors(ruleOrErrors);
        metadataRef.set(new Metadata(attributeDefinitions, rules));
    }

    public void setAttributesDefinitions(Map<String, Integer> attrDefs) throws InvalidRuleStructure {
        requireNonNull(attrDefs);
        Map<String, AttributeDefinition> attributeDefinitions = createAttributeDefinitions(attrDefs);
        List<Rule> rules = metadataRef.get().rules;
        validateRules(rules, attributeDefinitions);
        metadataRef.set(new Metadata(attributeDefinitions, rules));
    }

    public void setRulesAndAttributeDefinitions(Map<String, Integer> attrDefs, Map<String, String> idRuleMap) {
        requireNonNull(attrDefs);
        requireNonNull(idRuleMap);

        Map<String, AttributeDefinition> attributeDefinitions = createAttributeDefinitions(attrDefs);
        List<RuleOrError> ruleOrErrors = parseRules(attributeDefinitions, idRuleMap);
        List<Rule> rules = checkForErrors(ruleOrErrors);

        metadataRef.set(new Metadata(attributeDefinitions, rules));
    }

    private List<Rule> checkForErrors(List<RuleOrError> ruleOrErrors) {
        List<Rule> rules = new ArrayList<>(ruleOrErrors.size());
        List<RuleOrError> errors = new ArrayList<>(ruleOrErrors.size());
        for (RuleOrError ruleOrError : ruleOrErrors) {
            if (ruleOrError.isRule()) {
                rules.add(ruleOrError.getRule());
            } else if (ruleOrError.isError()) {
                errors.add(ruleOrError);
            }
        }

        if (!errors.isEmpty()){
            String errorMsg = errors.stream()
                    .map(roe -> "rule id: " + roe.getRuleId() + ", rule: " + roe.getRuleStr() + " has error: " + roe.getError().getMessage()).collect(Collectors.joining(","));
            throw new RuleEngineException("The following rules has errors: "+ errorMsg);
        }
        return rules;
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
        Metadata metadata = metadataRef.get();
        List<RuleAttribute> queryAttributes = Rule.queryFromMap(queryAttrs, metadata.attributeDefinitions, nameTransformator, valueTransformator);
        return query(metadata.rules, queryAttributes);
    }

    public String query(String queryAttrsStr) throws InvalidRuleStructure {
        Metadata metadata = metadataRef.get();
        List<RuleAttribute> queryAttributes = Rule.queryFromString(queryAttrsStr, metadata.attributeDefinitions, nameTransformator, valueTransformator);
        return query(metadata.rules, queryAttributes);
    }

    public Function<String, String> getNameTransformator() {
        return nameTransformator;
    }

    public Function<String, String> getValueTransformator() {
        return valueTransformator;
    }

    private void validateRules(List<Rule> rules, Map<String, AttributeDefinition> attributeDefinitions) {
        Set<String> definitionsNames = attributeDefinitions.keySet();
        for (Rule rule : rules) {
            Set<String> ruleNames = rule.getAttributes().stream().map(RuleAttribute::getName).collect(Collectors.toSet());
            boolean containsAll = definitionsNames.containsAll(ruleNames);
            if (!containsAll) {
                ruleNames.removeAll(definitionsNames);
                throw new RuleEngineException(String.format("Attribute definitions do not have definition for attribute(s) '%s' in rule '%s'", ruleNames, rule.asString()));
            }
        }
    }

    private List<RuleOrError> parseRules(Map<String, AttributeDefinition> attributeDefinitions,
                                  Map<String, String> idRuleMap) throws InvalidRuleStructure {
        List<RuleOrError> results = new ArrayList<>(idRuleMap.size());
        for (Map.Entry<String, String> entry : idRuleMap.entrySet()) {
            try {
                Rule rule = parseRule(attributeDefinitions, entry.getKey(), entry.getValue());
                results.add(RuleOrError.rule(rule));
            } catch (Exception e) {
                results.add(RuleOrError.error(entry.getKey(), entry.getValue(), e));
            }
        }
        return results;
    }

    private Rule parseRule(Map<String, AttributeDefinition> attributeDefinitions, String id, String ruleStr) throws InvalidRuleStructure {
        return Rule.ruleFromString(id, ruleStr, attributeDefinitions, nameTransformator, valueTransformator);
    }

    private String query(List<Rule> currentRules, List<RuleAttribute> queryAttributes) {
        List<RuleResult> possibleResults = new ArrayList<>();
        RuleResult notEqualResult = RuleResult.notEqual(null);
        possibleResults.add(notEqualResult);
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
