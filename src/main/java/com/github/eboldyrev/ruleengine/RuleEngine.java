package com.github.eboldyrev.ruleengine;

import com.github.eboldyrev.ruleengine.attributes.RuleAttribute;
import com.github.eboldyrev.ruleengine.exception.InvalidRuleStructure;
import com.github.eboldyrev.ruleengine.exception.MultiplyRulesFound;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuleEngine {

    private AtomicReference<List<Rule>> rulesRef = new AtomicReference<>(new ArrayList<>());
    private Function<String, String> valueTransformator = null;

    public RuleEngine() {
    }

    public RuleEngine(Function<String, String> valueTransformator) {
        this.valueTransformator = valueTransformator;
    }

    public Rule parseRule(String ruleStr){
        return Rule.ruleFromString(ruleStr, valueTransformator);
    }

    public List<Rule> parseRules(Set<String> ruleStrs) throws InvalidRuleStructure {
        List<Rule> rules = new ArrayList<>(ruleStrs.size());
        for (String ruleStr : ruleStrs) {
            Rule rule = parseRule(ruleStr);
            rules.add(rule);
        }
        return rules;
    }

    public void setRules(Set<String> ruleStrs){
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
        List<RuleAttribute> queryAttributes = Rule.queryFromString(queryAttrs, valueTransformator);

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

    public static void main(String[] args) {
        // sorted attr list
        // BrandId:Avaya#CountryId:US#RcAccountId:12345=Avaya_US_1
        // BrandId:Avaya#CountryId:US=Avaya_US_default
        // BrandId:Avaya#CountryId:Germany=Avaya_Germany_default
        // BrandId:Avaya#CountryId:Germany#RcAccountId:332211=Avaya_Germany_1

        RuleEngine ruleEngine = new RuleEngine();
        Set<String> ruleStrs = new HashSet<>();
        ruleStrs.add("BrandId:Avaya#CountryId:Germany=Avaya_Germany_1");
        ruleStrs.add("BrandId:Avaya=Avaya_Others_default");
        ruleStrs.add("BrandId:Avaya#CountryId:US#RcAccountId:12345=Avaya_US_1");
        ruleStrs.add("BrandId:Avaya#CountryId:US=Avaya_US_default");
        ruleStrs.add("CountryId:Germany=Germany_default");
        ruleStrs.add("RcAccountId:112233=Avaya_Germany_2");
        ruleStrs.add("RcAccountId:*=US_EAST2");
        ruleStrs.add("Domain:acme.com=US_EAST1");
        ruleStrs.add("Domain:*=US_EAST2");
        ruleStrs.add("Domain:nordigy*=nordigy_instance");
        ruleEngine.setRules(ruleStrs);

        String queryStr;
        String result;

//        queryStr = "BrandId:Avaya#CountryId:Germany#RcAccountId:222222";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Megafon#CountryId:Russia#RcAccountId:1111";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);

        queryStr = "Domain:nordigy1.com#RcAccountId:1111";
        result = ruleEngine.query(queryStr);
        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Avaya#CountryId:US#RcAccountId:123";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Avaya#CountryId:Russia#RcAccountId:12";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "BrandId:Avaya#CountryId:Germany#RcAccountId:33";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "Domain:yandex.ru";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);
//
//        queryStr = "RcAccountId:1";
//        result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);

//        String queryStr = "BrandId:hui#CountryId:Germany#RcAccountId:1";
//        String result = ruleEngine.query(queryStr);
//        System.out.println("Query: " + queryStr + " -> " + result);

        queryStr = "BrandId:hui#CountryId:Germany#Tier:1";
        result = ruleEngine.query(queryStr);
        System.out.println("Query: " + queryStr + " -> " + result);
    }

}
