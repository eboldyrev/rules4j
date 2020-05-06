package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.naturalOrder;

public class Rule {
    private static final String divider = "#";
    private static final String equalityDivider="=";

    List<RuleAttribute> attributes;
    private String result;
    private long weight;

    // todo add rule id -- to easily identify it!
    public Rule(List<RuleAttribute> attributes, String result) {
        this.attributes = attributes;
        this.weight = attributes.stream().map(RuleAttribute::getWeight).reduce(0, Integer::sum);
        this.result = result;
    }

    // BrandId:10#CountryId:1#RcAccountId:12345=1
    public static Rule ruleFromString(String rule){
        String[] ruleAndResult = rule.split(equalityDivider);
        if ( ruleAndResult.length !=2 ) {
            throw new IllegalArgumentException("No rule result found: " + rule);
        }

        return new Rule(queryFromString(ruleAndResult[0]), ruleAndResult[1]);
    }

    // BrandId:10#CountryId:1#RcAccountId:12345
    public static List<RuleAttribute> queryFromString(String queryStr){
        String[] splittedRule = queryStr.split(divider);
        Arrays.sort(splittedRule, naturalOrder());

        List<RuleAttribute> attributes = new ArrayList<>(splittedRule.length);
        for (String ruleAttrStr : splittedRule) {
            attributes.add(RuleAttribute.fromString(ruleAttrStr));
        }
        return attributes;
    }

    public RuleResult execute(List<RuleAttribute> queryAttributes){
        if (this.attributes.size() > queryAttributes.size()){
            return RuleResult.notApplicable(this);
        }

        boolean calculationResult = true;
        int queryAttrOffset = 0;
        for (int i=0; i< attributes.size(); i++){
            boolean foundFlag = true;
            while (!queryAttributes.get(queryAttrOffset + i).getName().equals(attributes.get(i).getName()) ) {
                queryAttrOffset ++;
                if (queryAttrOffset + i == queryAttributes.size()){
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return attributes.equals(rule.attributes) &&
                result.equals(rule.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, result);
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
