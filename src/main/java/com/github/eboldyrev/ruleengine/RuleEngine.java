package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.AttributeDefinition;
import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.eboldyrev.ruleengine.utils.Utils.nonEmpty;

public class RuleEngine {

    private final AtomicReference<List<Rule>> rulesRef = new AtomicReference<>(new ArrayList<>());
    private final Function<String, String> nameTransformator;
    private final Function<String, String> valueTransformator;
    private final Map<String, AttributeDefinition> attributeDefinitions;

    public RuleEngine(Map<String, Integer> attributeDefinitions,
                      Function<String, String> nameTransformator,
                      Function<String, String> valueTransformator) {
        nonEmpty(attributeDefinitions);
        this.attributeDefinitions = new HashMap<>((int)(attributeDefinitions.size() / 0.75));
        this.nameTransformator = nameTransformator;
        this.valueTransformator = valueTransformator;

        for (Map.Entry<String, Integer> definition : attributeDefinitions.entrySet()) {
            String key = nameTransformator != null ? nameTransformator.apply(definition.getKey()) : definition.getKey();
            this.attributeDefinitions.put(key, new AttributeDefinition(key, definition.getValue()));
        }
    }

    public Rule parseRule(String ruleStr){
        return Rule.ruleFromString(ruleStr, attributeDefinitions, nameTransformator, valueTransformator);
    }

    // TODO support Collection as argument ??
    // TODO return Set ??
    public List<Rule> parseRules(List<String> ruleStrs) throws InvalidRuleStructure {
        List<Rule> rules = new ArrayList<>(ruleStrs.size());
        for (String ruleStr : ruleStrs) {
            Rule rule = parseRule(ruleStr);
            rules.add(rule);
        }
        return rules;
    }

    public void setRules(List<String> ruleStrs){
        rulesRef.set(parseRules(ruleStrs));
    }

    public List<Rule> getRules(){
        return Collections.unmodifiableList(rulesRef.get());
    }

    public Set<String> getRulesAsStrings(){
        List<Rule> rules = rulesRef.get();
        Set<String> result = new HashSet<>(rules.size());
        for (Rule rule : rules) {
            result.add(rule.asString());
        }
        return result;
    }

    public String query(String queryAttrs){
        List<RuleAttribute> queryAttributes = Rule.queryFromString(queryAttrs, attributeDefinitions, nameTransformator, valueTransformator);

        List<RuleResult> possibleResults = new ArrayList<>();
        RuleResult notEqualResult = RuleResult.notEqual(null);
        possibleResults.add(notEqualResult);
        List<Rule> currentRules = rulesRef.get();
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

}
